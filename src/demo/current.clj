(ns demo.current
  (:require
   [missionary.core :as m]))

;; util

(def printers (atom []))

(defn print-flow! [f label]
  (let [print-t (m/reduce (fn [_r v]
                            (println label v)
                             v) nil f)
        dispose-f (print-t #(println "flow msg: " label %)
                           #(prn ::crash %))]
    (swap! printers conj dispose-f)))

;; v1 with atom

(def a1 (atom nil))

(def f1 (m/watch a1))

(print-flow! f1 "f1: ")

@printers

(defn send-msg1 [msg] 
  (reset! a1 msg))


(send-msg1 1)

(send-msg1 2)

(defn current-value [f]
  (->>  f
        (m/reductions (fn [r x] x) 0)
        (m/relieve {})
        (m/reduce (fn [_r v]
                    (println "cur: " v) v) nil)))

(m/? (current-value f1))




(defn msg-flow [!-a]
  ; without the stream the last subscriber gets all messages
  (m/stream
   (m/observe
    (fn [!]
      (reset! !-a !)
      (fn []
        (reset! !-a nil))))))


(defn flow-sender
  "returns {:flow f
            :send s}
    (s v) pushes v to f."
  []
  (let [!-a (atom nil)]
    {:flow (msg-flow !-a)
     :send (fn [v]
             (when-let [! @!-a]
               (! v)))}))


(def fs (flow-sender))

(def send-msg (:send fs))

(def f (:flow fs))




(send-msg 1)


(def current-value
  (->>  f
        (m/reductions (fn [r x] x) 0)
        (m/relieve {})
        (m/reduce (fn [_r v]
                    (println "cur: " v) v) nil)))

(m/? current-value)


(def op-cont (m/relieve (p/open-position-f pm)))



(m/? wo3)

(def dispose!
  (wo3 #(prn ::success %)
       #(prn ::crash %)))

(def dispose2!
  (op3 #(prn ::success %)
       #(prn ::crash %)))
