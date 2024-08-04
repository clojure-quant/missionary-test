(ns demo.connection.stream
  (:require
   [missionary.core :as m]
     [nano-id.core :refer [nano-id]]
   )
  )


(defn take-first-non-nil [f]
  ; flows dont implement deref
  (m/eduction
   (remove nil?)
   (take 1)
   f))


(defn current-v
  "gets the first non-nil value from the flow"
  [f]
  (m/reduce (fn [_r v]
              (println "current-v: " v)
              v) nil
            (take-first-non-nil f)))


(m/? (current-v
     (m/seed [nil nil nil 1 2 3])))
;; => 1

(m/? (current-v
      (m/stream (m/seed [nil nil nil 1 2 3]))))
;; => 1

(m/? (current-v
      (m/signal (m/seed [nil nil nil 1 2 3]))))
;; => 3


(def >conn
  (m/signal
   (m/ap
    (println "#### creating conn")
    (loop [c (nano-id 4)]
      (m/amb
       c
       (do (m/? (m/sleep 10000 c))
           (println "#### re-connecting .. ")
           (recur (nano-id 4))))))))


(m/? (current-v >conn))

(m/?
  (m/join vector (current-v >conn)
                 (m/sleep 1000 (m/? (current-v >conn)))
                 (current-v >conn)
          ))
