(ns demo.message.rdv-backpressure
   (:require
    [missionary.core :as m]
    [mutil.flow :refer [forever]]))


(defn print-drain [f]
  (m/reduce println f))

(defn print-call [t]
  (t println println))

(defn rdv-flow [r]
  (forever r))


(def r (m/rdv))
(def cancel (print-call (print-drain (rdv-flow r))))
(m/? (r "val 1"))
;; prints nil val 1, blocks until flow is ready to accept new value
(cancel)