(ns demo.batch-combiner
   (:require [missionary.core :as m]))

(defn forever [task]
  (m/ap (m/? (m/?> (m/seed (repeat task))))))

(def a-t
  (m/sp (m/? (m/sleep 50 :a))))

(def a-f
  ; gets run in regular intervals
  (m/ap (m/?> (forever a-t))))


(def b-t
  (m/sp  (let [r (rand-int 20)]
           (m/? (m/sleep r r)))))

(def b-f 
  ; produces data with different frequencies
  (m/ap (m/?> (forever b-t))))


(m/? (->> b-f
          (m/eduction 
           (take 5))
          (m/reduce conj)
          ))

(m/? (->> a-f
          (m/eduction
           (take 2))
          (m/reduce conj)))


(m/? (->> (m/zip vector a-f b-f)
          (m/eduction 
             (take 3))
          (m/reduce conj)
      ))
;; => [[:a 8] [:a 15] [:a 9]]

;; problem with this is that b and a need to be
;; synced, and when we read a we want all 
;; values of b.

(defn batch-combiner [r v]
  ;(println "batch combiner: " r v)
  (if (number? r)
    {:b [r v]}
    (update r :b conj v)))

(def b-f-batched (m/relieve batch-combiner b-f))


(m/? (->> (m/zip vector a-f b-f-batched)
          (m/eduction
           (take 5))
          (m/reduce conj)))
;; => [[:a {:b [17 16 2]}]
;;     [:a {:b [16 3 13 1 8 16]}]
;;     [:a {:b [12 18 0 8 9]}]
;;     [:a {:b [15 5 8 9 12 0 4]}]
;;     [:a {:b [2 2 16 9 14]}]]





