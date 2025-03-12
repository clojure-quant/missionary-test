(ns demo.switch 
  (:require 
   [missionary.core :as m]))

(defn switch [a f]
  (m/ap
   (let [a-f (m/stream (m/watch a))
         a-cur (m/?> a-f)]
     (if a-cur
       a-cur
       (m/?> f)))))

; example

(def >clock   
  (m/stream
   (m/ap
    (println "creating clock!")
    (loop [i 0]
      (m/amb
       (m/? (m/sleep 1000 i))
       ;(println "i: " i)
       (recur (inc i)))))))

; in beginning we want to use a val
(def dt (atom :a))

(def dispose! 
((m/reduce (fn [r v] (println "dag time: " v))
           nil
           (switch dt >clock))
 #(println "success " %)
 #(println "error " %))  
  )

; now switch to clock-flow
(reset! dt nil)

; now switch back to a val
(reset! dt :b)

; error is, it does not switch back to a-val
