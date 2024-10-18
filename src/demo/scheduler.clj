(ns demo.scheduler
  (:require
   [tick.core :as t]
   [missionary.core :as m]))



; reduce a fixed size flow
(m/? (m/reduce println nil scheduler))







(comment 

;; you need to wait 15 seconds after evaling !!
;; => [nil
; scheduler sleeping for seconds:  0  until:  #time/instant "2024-07-11T23:55:33.260644629Z"
; schedule was from the past:  #time/instant "2024-07-11T23:55:33.260644629Z"
; scheduler sleeping for seconds:  5  until:  #time/instant "2024-07-11T23:55:38.260644629Z"
[:past 
 #time/instant "2024-07-11T23:55:38.260644629Z" 
 #time/instant "2024-07-11T23:55:43.260644629Z"]


; process 10 events (takes 50 seconds)
 (m/? 
   (let [seq (take 10 (every-5-seconds))
         s (scheduler seq)]
     (m/reduce (fn [& [r t]]
                 (println "[10 events only] processing time: " t " run: " r)
                 (if r (inc r) 1)
                 ) (m/signal s))))


(m/? 
  (let [seq (every-5-seconds)
       s (scheduler seq)]
   (m/reduce (fn 
               [& args ]
               ;(println "[infinite events]  processing time: " t " run: " r)
               (println "[infinite events]  args: " args)
               ;(if r (inc r) 1)
               )
             (m/signal s))))
 

 (reduce 
   (fn [r t]
    (println "[infinite events]  processing time: " t " run: " r)
    (if r (inc r) 1)) 
   (range 5)
  )

;
)