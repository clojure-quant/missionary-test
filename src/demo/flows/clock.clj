(ns demo.flows.clock
  (:require
   [missionary.core :as m]))

(def >clock    ;; A shared process emitting increasing numbers every second.
  (m/stream
   (m/ap
    (println "creating clock!")
    (loop [i 0]
      (m/amb
       (m/? (m/sleep 1000 i))
       ;(println "i: " i)
       (recur (inc i)))))))



(def stopping-clock
  (m/ap
   (println "creating clock!")
   (let [v (m/?> >clock)]
     (if (> (rand 100) 90.0)
       (do
         (println "stopping clock.")
         (reduced nil)) ;; we could return anything here. what counts is the reducing function must return (reduced)
       v))))

(defn print-while-not-reduced [s v]
  (println "value: " v)
  v)

(m/? (m/reduce print-while-not-reduced nil stopping-clock))

