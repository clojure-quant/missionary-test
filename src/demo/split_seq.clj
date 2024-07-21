(ns demo.split-seq
  (:require
   [missionary.core :as m]))

; RAW

(def raw-order-flow
  (m/seed '([1 2 3]
            [4 5])))

(m/? (m/reduce conj raw-order-flow))

; helper fn

(defn split-seq-flow [s]
  (m/ap
   (loop [s s]
     (m/amb
      (first s)
      (let [s (rest s)]
        (when (seq s)
          (recur s)))))))

(def order-flow
  (m/ap
   (let [data (m/?> raw-order-flow)]
     (m/?> (split-seq-flow data)))))

(m/? (m/reduce conj order-flow))
;; => [1 2 3 nil 4 5 nil]

