(ns demo.scheduler
  (:require
   [tick.core :as t]
   [missionary.core :as m]))

(defn periodic-seq [start duration]
  (iterate #(t/>> % duration) start))

(defn every-5-seconds []
  (periodic-seq
   (t/now)
   (t/new-duration 5 :seconds)))

(take 3 (every-5-seconds))
;; => (#time/instant "2024-07-07T21:36:04.200075828Z"
;;     #time/instant "2024-07-07T21:36:09.200075828Z"
;;     #time/instant "2024-07-07T21:36:14.200075828Z")


(def seq1  (periodic-seq
               (t/now)
               (t/new-duration 5 :seconds)))

(def  input (m/seed seq1))

(def scheduler 
  "returns a missionary flow"
   (m/ap
     (let [next-time (m/?> input)
           time (t/now)
           diff (- (t/long next-time) (t/long time))
           diff-ms (* 1000 diff)]
       (println "scheduler sleeping for ms: " diff-ms " until: " next-time)
       (if (> diff-ms 0)
         (do (println "sleeping ms" diff-ms)
             (m/? (m/sleep diff-ms next-time))
             (println "finished sleeping")
             next-time)
         (do (println "schedule was from the past: " next-time)
             :past
             )))))

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