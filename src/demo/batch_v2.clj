(ns demo.batch-v2
  (:require
   [missionary.core :as m]
   [demo.flows.producer :refer [data-producer]]))

(def producer-slow (data-producer 200))
(def producer-fast (data-producer 30))

((->> (m/ap
       (let [[_ batch] (m/?> (m/group-by {} (m/ap (m/?> (m/amb= producer-slow producer-fast)))))]
         (m/? (->> (m/ap (m/amb= (m/?> batch)
                                 (m/? (m/sleep 50))))
                   (m/eduction (take-while some?))
                   (m/reduce conj)))))
      (m/eduction (take 20))
      (m/reduce conj))
 prn prn)
;; => #object[missionary.impl.Reduce$Process 0x3a3c9dd0 "missionary.impl.Reduce$Process@3a3c9dd0"]

[[0 0 1 2] [3 4 5] [6 1 7 8 2 9 10] [11 12 13 14 3]
 [15 16 4 17 18 19 20 21] [22 23 24 5 25]
 [26 6 27 28 29] [30 31 32 33 34 35] [36 37 7 38 39 40]
 [41 42 43] [44 45 46 47 48 49] [50 8 51 52 53] [54 55 56]
 [57 58 59] [9 60 61 62] [63 64 65 66 10 67] [11 68 69 12 70 13 71]
 [72 73 74 75 14] [76 77 78 79 80 81 82] [15 83 84 85 86]]

; the essence of the pattern is m/group-by, which splits a flow over space
; (i.e. partition the value set according to a key function) and 
; time (i.e. cancelling a group consumer starts a new batch) . 
; In this case we're not interested in the space partition so we use {} as 
; the key function, i.e. all values have the same key. 
; The time partition is defined by the m/eduction stage which early terminates 
; when the m/sleep result is observed

; m/amb= is just sugar over m/?>, you can look the source







(defmacro
  ^{:arglists '([& forms])
    :doc "In an `ap` block, evaluates each form concurrently and returns results in order of availability."}
  amb=
  ([] `(?> none))
  ([form] form)
  ([form & forms]
   (let [n (inc (count forms))]
     `(case (?> ~n (seed (range ~n)))
        ~@(interleave (range) (cons form forms))))))


(def main-f
  (m/ap
   (loop [x 1]
     (m/amb
      [:main x]
      (do (m/? (m/sleep 100))
          (recur (inc x)))))))


(m/? (->>  main-f
           (m/eduction (take 3))
           (m/reduce conj nil)))

((->> (m/ap
       (let [[_ batch] (m/?> (m/group-by {} (m/ap (m/?> (m/amb= producer-slow producer-fast)))))]
         (m/? (->> (m/ap (m/amb= (m/?> batch)
                                 (m/?> main-f)))
                   (m/eduction (take-while some?))
                   (m/reduce conj)))))
      (m/eduction (take 5))
      (m/reduce conj))
 prn prn)



(defn batch-process [main-flow & client-flows]
  (m/ap
   (let [[_ batch] (m/?> (m/group-by {} (m/ap (m/?> (apply m/amb= client-flows)))))]
     (m/? (->> (m/ap (m/amb= (m/?> batch)
                             (m/? (m/sleep 50))))
               (m/eduction (take-while some?))
               (m/reduce conj))))))