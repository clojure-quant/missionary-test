(ns demo.stream
  (:require
   [missionary.core :as m])
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

