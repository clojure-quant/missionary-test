(ns demo.clock
  (:require
   [missionary.core :as m]))

(def >clock    ;; A shared process emitting `nil` every second.
  (m/stream
   (m/ap
    (loop [i 0]
      (m/amb
       (m/? (m/sleep 1000))
       (println "i: " i)
       (recur (inc i)))))))


(defn counter [r _] (inc r)) 
;; A reducing function counting the number of items.

((m/join vector
         (m/reduce counter 0 (m/eduction (take 6) >clock))
         (m/reduce counter 0 (m/eduction (take 30) >clock)))
 prn prn)





 ;; After 4 seconds, prints [3 4]

(m/?
 (m/reduce counter 0 (m/eduction (take 30) >clock)))


;; => 3


;; this fails:

(m/? 
(m/eduction (take 3) >clock) 
 )


(m/?
 (m/reduce (fn [r v] 
             (println "tick: " v))
           >clock))
 





