(ns demo.position
  (:require
   [tick.core :as t]
   [missionary.core :as m]
   [demo.scheduler :refer [scheduler every-5-seconds]]))


(def time1 (m/seed [(t/instant "2024-07-11T23:55:33Z")
                    (t/instant "2024-07-12T23:55:33Z")
                    (t/instant "2024-07-13T23:55:33Z")]))

(def time2
  scheduler)


(def quotes1 (m/seed [{:price 1000}
                     {:price 1500}
                     {:price 1777}]))

(def quotes2  (m/stream
               (m/ap 
              (loop [p (rand-int 1000)]
                     (m/amb p)
                     (m/? (m/sleep 5000))
                     (recur (rand-int 10000))))))


(def position {;:asset "BTC"
               :entry-price 900
               ;:quantity 500
               :entry-date (t/instant "2024-07-10T23:55:33Z")})

(defn eval-position [time price]
  (assoc position
         :exit-price price
         :exit-date time
         ;:pl (- price (:entry-price position))
         ))

(m/?
 (m/reduce
  (fn [_ x] (prn "position eval: " x))
  nil
  (m/latest eval-position time1 quotes2)))


(m/?
 (m/reduce
  (fn [_ x] (prn "position eval: " x))
  nil
    quotes2))



(m/? 
 (m/reduce 
  (fn [_ x] (prn "position eval: " x))
  nil 
  (m/latest eval-position time1 quotes1)
             ))



(def main
  (let [<y (m/signal (m/latest eval-position time1 quotes2))]
    (m/reduce (fn [_ x] (prn "position eval: " x)) nil <y)))


(def dispose!
  (main
   #(prn ::success %)
   #(prn ::crash %)))

(dispose!)


(m/? (m/reduce (fn [_ s]
                 (println "current position: " s))
               nil position-eval))

; sending order:  {:order-id 1, :asset BTC, :side :buy}
; received trade:  {:trade-id 1, :asset BTC, :side :buy, :price 1000.0}
; received trade:  {:trade-id 1, :asset ETH, :side :buy, :price 300.0}
; data:  [{:order-id 1, :asset BTC, :side :buy} {:trade-id 1, :asset BTC, :side :buy, :price 1000.0}]
; sending order:  {:order-id 2, :asset ETH, :side :buy}
; received trade:  {:trade-id 1, :asset BTC, :side :buy, :price 1000.0}
; data:  [{:order-id 1, :asset BTC, :side :buy} {:trade-id 1, :asset ETH, :side :buy, :price 300.0}]
; received trade:  {:trade-id 1, :asset ETH, :side :buy, :price 300.0}
; data:  [{:order-id 2, :asset ETH, :side :buy} {:trade-id 1, :asset BTC, :side :buy, :price 1000.0}]
; data:  [{:order-id 2, :asset ETH, :side :buy} {:trade-id 1, :asset ETH, :side :buy, :price 300.0}]
nil



  
  

