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
         ;; this is not working!!
         (reduced nil))
       v))))

(m/? (m/reduce println stopping-clock))

