(ns demo.message.rendevouz
   (:require
    [missionary.core :as m]))
  
;Creates an instance of synchronous rendez-vous.
;A synchronous rendez-vous is a function implementing
;  `give` on its 1-arity and 
; `take` on its 2-arity (as task).
; `give` takes a value to be transferred and returns a task 
; completing with nil as soon as a taker is available. 
; `take` is a task completing with transferred value as soon 
; as a giver is available.

;Cancelling `give` and `take` tasks makes them fail immediately.

;Example : producer / consumer stream communication

(defn reducer [rf i take]
  (m/sp
    (loop [r i]
      (let [x (m/? take)]
        (if (identical? x take)
          r 
          (recur (rf r x))))))) 

(defn iterator [give xs]
  (m/sp
    (loop [xs (seq xs)]
      (if-some [[x & xs] xs]
        (do (m/? (give x))
            (recur xs))
        (m/? (give give))))))

;; example synchronous

(reduce + 0 (range 100))
;; => 4950

; example async

(def stream (m/rdv))

(m/? (m/join {} 
             (iterator stream (range 100))
             (reducer + 0 stream)))
;; => 4950

(m/? (m/join vector
             (iterator stream (range 100))
             (reducer + 0 stream)))
;; => [nil 4950]


