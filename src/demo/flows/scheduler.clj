(ns demo.flows.scheduler
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
           :past)))))