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

(let [n (t/now)
      nl (t/long n)
      f (t/>> n (t/new-duration 1 :seconds))
      fl (t/long f)]
  (- fl nl))
; t/long difference is in seconds


(defn scheduler [seq]
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
;; => [nil
;;     #time/instant "2024-07-07T17:06:53.900328228Z"
;;     #time/instant "2024-07-07T17:06:55.900328228Z"
;;     #time/instant "2024-07-07T17:06:57.900328228Z"
;;     #time/instant "2024-07-07T17:06:59.900328228Z"]


;; NOT WORKING FROM HERE.

(defn print-time [flow]
 (m/ap
  (let [time (m/?> flow)]
    (println "processing time: " time))))


(let [seq (take 3 (every-5-seconds))
      s (scheduler seq)
      print (print-time s)]
  (m/? print))
