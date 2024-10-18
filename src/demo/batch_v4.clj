(ns demo.batch-v4
  (:require
   [missionary.core :as m]
   [mutil.task-runner :as tr]
   [demo.flows.producer :refer [data-producer]]))

;; worker

(def r (m/rdv))

(defn action! [a]
  (m/? (r a)))

(def worker-f
  (m/ap
   (loop [i 0]
     (let [v (m/? r)]
       (m/amb
        [v i]
        (recur (inc i)))))))

(tr/start-flow-printer! worker-f :worker)

(tr/stop! :worker)

(action! :wow)


;; jobs

(def jobs-f (m/seed [:a :b :c :d :e :f ::end]))

(defn delay-each [delay input]
  (m/ap (m/? (m/sleep delay (m/?> input)))))

(def jobs-delayed-f (delay-each 5 jobs-f))

((->> jobs-delayed-f
      (m/eduction (take 2))
      (m/reduce conj))
 prn prn)

((->> jobs-delayed-f
      (m/eduction (take-while #(not= % ::end)))
      (m/reduce conj))
 prn prn)

(def producer-fast (data-producer 5))

;; processor

(def batched-f
  (m/ap
   (let [;restartable (second (m/?> (m/group-by {} worker-f)))
         restartable2 (second (m/?> (m/group-by {} producer-fast)))]
     (m/? (->> (m/ap (m/amb= ;(m/?> restartable)
                      (m/?> restartable2)
                             ;(m/?> jobs-delayed-f)
                      (m/? (m/sleep 5 ::end))))
               (m/eduction (take-while #(not= % ::end)))
               (m/reduce conj))))))

((->> batched-f
      (m/eduction (take 3))
      (m/reduce conj))
 prn prn)

  ;position-change-flow (m/buffer 100 position-change-flow)

(defn process-job [job]
  (case job
    :b (do (action! :BBB) :scheduled-b)
    :d (do (action! :DDD) :scheduled-d)
    :nothing-to-be-done))

(tr/start-flow-printer! worker-f :worker)

(process-job :a)
;; => :nothing-to-be-done

(process-job :b)
;; => :scheduled-b
;; => :scheduled-b
;; => :scheduled-b

(def work-processor
  (m/ap
   (let [job (m/?> jobs-delayed-f)]
     (process-job job))))

((->> work-processor
      (m/eduction (take 3))
      (m/reduce conj))
 prn prn)
; :worker   [:BBB 18]
; [:nothing-to-be-done :scheduled-b :nothing-to-be-done]

(def work-processor2
  (m/ap
   (let [restartable (second (m/?> (m/group-by {} worker-f)))]
     ;(process-job job) ; this is blocking
     (m/? (->> (m/ap (m/amb=
                      (m/?> restartable)
                      (m/?> jobs-delayed-f)))
               (m/eduction (take-while #(not= % ::end)))
               (m/reduce conj))))))


((->> work-processor2
      (m/eduction (take 3))
      (m/reduce conj))
 prn prn)



