(ns demo.backpressure
   (:require
    [missionary.core :as m]))


(defn print-drain [f]
  (m/reduce println f))

(defn print-call [t]
  (t println println))

(defn forever [task]
  (m/ap (m/? (m/?> (m/seed (repeat task))))))


(defn rdv-flow [rdv]
  (forever rdv))

(comment
  (def rdv (m/rdv))
  (def cancel (print-call (print-drain (rdv-flow rdv))))
  (m/? (rdv "val 1")) ;; prints nil val 1, blocks until flow is ready to accept new value
  (cancel))