(ns demo.batch-rapid
   (:require [missionary.core :as m]))

  (defn data-producer [max-delay-ms]
    (m/ap
     (loop [i 0]
       (m/amb
        (m/? (m/sleep (rand-int max-delay-ms) i))
        (recur (inc i))))))
  
 (def producer-fast (data-producer 5))


  (defn batch-combiner [r v]
    (if (vector? r)
      (conj r v)
      [r v]))
      
  (def producer-fast-batched (m/relieve batch-combiner producer-fast))


(def entry-signal (m/seed [1 2 3 4 5 6]))

  
(def entry-action 
  (m/ap 
     (let [s (m/?> entry-signal)]
       (m/? (m/sleep 10))
       s)))


(m/? (->> (m/sample vector
                    (m/reductions {} :nil producer-fast-batched)
                    entry-action)
          (m/reduce conj)))