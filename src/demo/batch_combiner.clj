(ns demo.batch-combiner
   (:require [missionary.core :as m]))

(defn forever [task]
  (m/ap (m/? (m/?> (m/seed (repeat task))))))

;; dataproducer slow

(def a-t
  (m/sp  (let [r (rand-int 100)]
           (m/? (m/sleep r r)))))

(def a-f
  ; produces data with different frequencies
  (m/ap (m/?> (forever a-t))))

; dataproducer fast

(def b-t
  (m/sp  (let [r (rand-int 20)]
           (m/? (m/sleep r r)))))

(def b-f 
  ; produces data with different frequencies
  (m/ap (m/?> (forever b-t))))


;; combiner task

(def c-t
  (m/sp (m/? (m/sleep 50 :c))))

(def c-f
  ; gets run in regular intervals
  (m/ap (m/?> (forever c-t))))



(m/? (->> b-f
          (m/eduction 
           (take 5))
          (m/reduce conj)
          ))

(m/? (->> a-f
          (m/eduction
           (take 2))
          (m/reduce conj)))


(m/? (->> (m/zip vector a-f b-f c-f)
          (m/eduction 
             (take 3))
          (m/reduce conj)
      ))

;; => [[35 0 :c] [53 11 :c] [10 9 :c]]

;; problem with this is that b and a need to be
;; synced, and when we read c we want all 
;; values of a and all values of b.

(defn batch-combiner [r v]
  (if (vector? r)
    (conj r v)
    [r v]))

(defn f-or-nil2 [f]
  (m/reductions (fn [r v]
                (if v v r)) nil f))

(def a-f-batched (m/relieve batch-combiner a-f))

(def b-f-batched (m/relieve batch-combiner b-f))

(m/? (->> (m/zip vector c-f 
                 (f-or-nil2 a-f-batched )
                 b-f-batched)
          (m/eduction
           (take 20))
          (m/reduce conj)))

;; the below example is not quite what we want,
;; for the first row :a did not submit a value, and therefore
;; is included with nil. this is ok. 
;; but for all subsequent rows it would block until a is available.

;; => [[:c nil [9 16 18 1]]
;;     [:c 96 [9 2 8 18 17]]
;;     [:c 94 [6 19 6 16 10 16 14]]
;;     [:c 83 [4 2 6 0 11 12 4 5 13 15 1]]
;;     [:c 51 [17 17 18 2 0]]
;;     [:c 6 11]
;;     [:c 76 [0 6 14 6 19 1 18]]
;;     [:c 12 [16 8]]
;;     [:c 64 [12 4 14 6 10]]
;;     [:c 92 [16 0 6 6 19 4 4 17 7 13 11]]
;;     [:c 84 [15 8 17 5 4 15 5 9]]
;;     [:c 72 [18 0 12 16 15 11]]
;;     [:c 74 [19 5 7 8 13 13]]
;;     [:c 74 [16 5 18 2 8 8 17]]
;;     [:c 49 [15 11 11 13 5]]
;;     [:c 68 [15 16 0 16 6 0 13 0]]
;;     [:c 67 [15 2 16 1 9 15 3]]
;;     [:c 37 [15 3 5 15]]
;;     [:c 70 [13 19 8 6 6 19]]
;;     [:c 62 [17 14 5 16 7 2]]]

(defn sleep-emit [delays]
  (m/ap (let [n (m/?> (m/seed delays))]
        (m/? (m/sleep n n)))))

(def clock 
   (m/ap
    (loop [i 0]
      (m/amb
       (m/? (m/sleep (rand-int 100) i))
       (recur (inc i))))))

(def clock-batched (m/relieve batch-combiner clock))

(m/? (->> (m/sample vector
                    (m/reductions {} :nil clock-batched)
                    (m/reductions {} :nil clock-batched)
                    (sleep-emit [100 50 30 20 100 4
                                 100 50 30 20 100 4
                                 100 50 30 20 100 4
                                 ]))
             (m/reduce conj)))

;; this is almost what we want.
;; we get the values batched that occured in the sampled
;; window. However if there was no value, we do get a 
;; repetition of the prior value. for discrete transation
;; processors not optimal.

;; => [[0 [0 1] 100]
;;     [1 [0 1] 50]
;;     [1 2 30]
;;     [1 2 20]
;;     [[2 3 4] [3 4 5 6] 100]
;;     [[2 3 4] [3 4 5 6] 4]
;;     [[5 6] 7 100]
;;     [[7 8] [8 9] 50]
;;     [[7 8] [8 9] 30]
;;     [[7 8] 10 20]
;;     [[9 10 11] [11 12 13] 100]
;;     [[9 10 11] [11 12 13] 4]
;;     [[12 13 14 15 16] [14 15 16 17 18] 100]
;;     [[17 18 19] [14 15 16 17 18] 50]
;;     [[17 18 19] 19 30]
;;     [[17 18 19] 19 20]
;;     [20 20 100]
;;     [20 20 4]]