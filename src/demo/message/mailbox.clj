(ns demo.message.mailbox
  (:require
   [missionary.core :as m]
   [mutil.task-runner :refer [start! stop!]]))

(def box (m/mbx))

(def worker
  (m/sp
   (loop [v (m/? box)]
     (println "worker: " v)
     (m/? (m/sleep 10000))
     (recur (m/? box)))))

(start! worker :worker)

(box :a)
(box :b)
(box "hello")

(stop! :worker)

(box :c)
(box :d)

(start! worker :worker)


;; conclusion: mailbox is unbounded.