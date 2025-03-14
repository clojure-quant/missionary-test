(ns demo.flows.failover
  (:require
   [missionary.core :as m])
  (:import [missionary Cancelled])
  )

(defn forever [task]
  (m/ap (m/? (m/?> (m/seed (repeat task))))))

(defn flow-selector [source-a flow-map]
  (let [source-change-f (m/watch source-a)
        process-same-source-t (fn [current-source !]
                                (println "processing source: " current-source)
                                (let [source-f (get flow-map current-source)]
                                  (m/race  (->> source-change-f
                                                (m/eduction 
                                                 (remove #(= current-source %))
                                                 (take 1))
                                                (m/reduce (fn [r v] v) nil))
                                           (m/reduce (fn [_s v] (! v)) nil source-f))))
        process-t (fn [!]
                    (let [current-a (atom @source-a)]
                      (m/sp
                       (let [new-source (m/? (process-same-source-t @current-a !))]
                         (reset! current-a new-source)))))]
    (m/observe
     (fn [!]
       (let [process-1 (process-t !)
             process-f (forever process-1)
             runner-t (m/reduce (fn [r v] nil) nil process-f)
             dispose (runner-t #(prn ::success %) #(prn ::crash %))]
         (fn []
           (dispose)))))))

;; DEMO

(defn >clock [next-fn]
  (m/stream
   (m/ap
    (println "creating clock!")
    (loop [i 0]
      (m/amb
       (m/? (m/sleep 5000 i))
       (recur (next-fn i)))))))

(def flow-map {:a (>clock inc)
               :b (>clock dec)})


(def source (atom :a))

(def failover (flow-selector source flow-map))


(def stop!
  ((m/reduce (fn [r v]
               (println "failover val: " v)) nil failover)
   #(prn ::client-success %)
   #(prn ::client-crash %)))

(reset! source :b)

(reset! source :a)

(stop!)


(defn switch [a flow-map]
  (m/ap
   (try (let [a-cur (m/?< (m/watch a))
              cur-f (get flow-map a-cur)]
          (m/?> cur-f))
        (catch Cancelled _ (m/amb)))))


(def source (atom :a))

(def flow-map {:a (>clock inc)
               :b (>clock dec)})


(def failover (switch source flow-map))


(def stop!
  ((m/reduce (fn [r v]
               (println "failover val: " v)) nil failover)
   #(prn ::client-success %)
   #(prn ::client-crash %)))

(reset! source :b)

(reset! source :a)

(stop!)