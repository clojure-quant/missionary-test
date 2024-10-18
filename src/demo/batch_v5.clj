(ns demo.batch-v5
   (:require
    [missionary.core :as m]))

(def main-f (m/seed [:a :b :c :d :e :f :g ::end]))

(def service-f (m/seed [1 2 3]))

(def work-processor
  (m/ap
   (let [restartable (second (m/?> (m/group-by {} service-f)))]
     ;(process-job job) ; this is blocking
     (m/? (->> (m/ap (m/amb=
                      (m/?> restartable)
                      (m/?> main-f)))
               (m/eduction (take-while #(not= % ::end)))
               (m/reduce conj))))))

((->> work-processor
      (m/eduction (take 2))
      (m/reduce conj))
 prn prn)

; [[:a 1 :b 2 :c 3 :d :e :f :g]]


(def r (m/rdv))

(defn action! [a]
  (println "action: " a)
  ;(m/? (r a))
  nil
  )

(def service2-f
  (m/ap
   (loop [i 0]
     (let [v (m/? r)]
       (m/amb
        [v i]
        (recur (inc i)))))))

(defn process-job [job]
  (case job
    :b (do (action! :BBB) :scheduled-b)
    :d (do (action! :DDD) :scheduled-d)
    :nothing-to-be-done))

(defn create-action-flow [f] 
  (m/ap
   (let [job (m/?> f)]
     (m/via m/cpu 
            (process-job job)       
            )
     )))


(def work-processor2
  (m/ap
   (let [restartable (second (m/?> (m/group-by {} service-f)))
         ;restartable (second (m/?> (m/group-by {} service2-f)))
         ;restartable2 (second (m/?> (m/group-by {} (create-action-flow main-f))))
         ]
     (m/? (->> (m/ap (m/amb=
                      (m/?> restartable)
                      ;(m/?> restartable2)
                      (m/?> main-f)))
               (m/eduction (take-while #(not= % ::end)))
               (m/reduce conj))))))

((->> work-processor2
      (m/eduction (take 2))
      (m/reduce conj))
 prn prn)

; [[:a 1 1 :b 2 2 :c 3 3 :d :e :f :g]]


