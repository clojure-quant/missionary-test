(ns demo.modify
  (:require
   [missionary.core :as m]))

; RAW

(def raw-order-flow
  (m/seed [1 2 3]))

(m/? (m/reduce conj raw-order-flow))

; helper fn

(defn wrap [f]
 (m/eduction (map (fn [r] {:order r})) f))

(m/? (m/reduce conj (wrap raw-order-flow)))
;; => [{:order 1} {:order 2} {:order 3}]



