(ns demo.flows.producer
   (:require [missionary.core :as m]))
  


(defn data-producer [max-delay-ms]
  (m/ap
   (loop [i 0]
     (m/amb
      (m/? (m/sleep (rand-int max-delay-ms) i))
      (recur (inc i))))))

