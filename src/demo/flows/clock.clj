(ns demo.flows.clock
   (:require
    [missionary.core :as m]))
  
(def >clock    ;; A shared process emitting `nil` every second.
  (m/stream
   (m/ap
    (println "creating clock!")
    (loop [i 0]
      (m/amb
       (m/? (m/sleep 1000 i))
       ;(println "i: " i)
       (recur (inc i)))))))


  