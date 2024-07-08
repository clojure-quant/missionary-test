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


(let [n (t/now)
      nl (t/long n)
      f (t/>> n (t/new-duration 1 :seconds))
      fl (t/long f)]
  (- fl nl))
; t/long difference is in seconds


(defn scheduler 
  "returns a missionary flow"
  [seq]
  (let [input (m/seed seq)]
    (m/ap
     (let [next-time (m/?> input)
           time (t/now)
           diff (- (t/long next-time) (t/long time))
           diff-ms (* 1000 diff)]
       (println "scheduler sleeping for seconds: " diff " until: " next-time)
       (if (> diff 0)
         (do (m/? (m/sleep diff-ms next-time))
             (println "finished sleeping")
             next-time)
         (println "schedule was from the past: " next-time))))))

; reduce a fixed size flow

(let [seq (take 3 (every-5-seconds))
      s (scheduler seq)]
  (m/? (m/reduce conj s)))
;; you need to wait 15 seconds after evaling !!
;; => [nil
;;     #time/instant "2024-07-07T17:06:53.900328228Z"
;;     #time/instant "2024-07-07T17:06:55.900328228Z"
;;     #time/instant "2024-07-07T17:06:57.900328228Z"
;;     #time/instant "2024-07-07T17:06:59.900328228Z"]


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



scheduler