(ns demo.batch-combiner
   (:require [missionary.core :as m]))

;; data-producer

(defn data-producer [max-delay-ms]
  (m/ap
   (loop [i 0]
     (m/amb
      (m/? (m/sleep (rand-int max-delay-ms) i))
      (recur (inc i))))))

(def producer-slow (data-producer 200))
(def producer-fast (data-producer 30))


;; data-consumer

(defn forever [task]
  (m/ap (m/? (m/?> (m/seed (repeat task))))))

(def c-t
  (m/sp (m/? (m/sleep 50 :c))))

(def consumer-f
  ; gets run in regular intervals
  (m/ap (m/?> (forever c-t))))

(defn batch-combiner [r v]
  (if (vector? r)
    (conj r v)
    [r v]))

(defn f-or-nil2 [f]
  (m/reductions (fn [r v]
                (if v v r)) nil f))

(def producer-slow-batched (m/relieve batch-combiner producer-slow))

(def producer-fast-batched (m/relieve batch-combiner producer-fast))

(m/? (->> (m/zip vector 
                 (f-or-nil2 producer-slow-batched )
                 producer-fast-batched
                 consumer-f)
          (m/eduction
           (take 20))
          (m/reduce conj)))

;; the below example is not quite what we want,
;; for the first row :a did not submit a value, and therefore
;; is included with nil. this is ok. 
;; but for all subsequent rows it would block until a is available.

;; => [[nil [0 1 2 3 4] :c]
;;     [0 [5 6 7 8 9 10 11 12 13] :c]
;;     [1 [14 15] :c]
;;     [2 [16 17 18 19 20 21 22 23 24 25 26 27 28 29] :c]
;;     [3 [30 31 32 33 34 35 36 37 38] :c]
;;     [4 [39 40 41 42 43 44 45 46 47] :c]
;;     [5 [48 49] :c]
;;     [6 [50 51 52 53 54 55 56 57 58] :c]
;;     [7 [59 60] :c]
;;     [8 [61 62 63 64 65 66] :c]
;;     [9 [67 68 69 70 71 72 73 74 75 76 77 78] :c]
;;     [10 [79 80 81 82] :c]
;;     [11 [83 84 85 86 87 88 89 90 91 92 93 94 95 96] :c]
;;     [12 97 :c]
;;     [13 [98 99 100 101 102 103 104 105 106 107] :c]
;;     [14 [108 109 110] :c]
;;     [15 [111 112 113] :c]
;;     [16 114 :c]
;;     [17 [115 116 117] :c]
;;     [18 [118 119 120 121 122 123 124 125 126 127 128 129] :c]]

(defn sleep-emit [delays]
  (m/ap (let [n (m/?> (m/seed delays))]
        (m/? (m/sleep n :c)))))

(m/? (->> (m/sample vector
                    (m/reductions {} :nil producer-slow-batched)
                    (m/reductions {} :nil producer-fast-batched)
                    (sleep-emit [100 50 30 20 100 4
                                 100 50 30 20 100 4
                                 100 50 30 20 100 4
                                 ])
                    ;consumer-f
                    )
           (m/reduce conj)))

;; this is almost what we want.
;; we get the values batched that occured in the sampled
;; window. However if there was no value, we do get a 
;; repetition of the prior value. for discrete transation
;; processors not optimal.

;; => [[0 [0 1 2 3 4 5] :c]
;;     [0 [6 7 8] :c]
;;     [0 [9 10 11] :c]
;;     [0 [9 10 11] :c]
;;     [1 [12 13 14 15 16 17 18 19 20] :c]
;;     [1 [12 13 14 15 16 17 18 19 20] :c]
;;     [2 [21 22 23 24 25 26 27] :c]
;;     [2 [28 29 30] :c]
;;     [3 31 :c]
;;     [3 32 :c]
;;     [4 [33 34 35 36 37 38 39 40] :c]
;;     [4 41 :c]
;;     [4 [42 43 44 45] :c]
;;     [5 [46 47] :c]
;;     [5 [48 49 50] :c]
;;     [5 51 :c]
;;     [5 [52 53 54 55 56 57 58 59] :c]
;;     [5 60 :c]]