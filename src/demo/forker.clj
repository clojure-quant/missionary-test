(ns demo.forker
  (:require
   [missionary.core :as m]))


(def orders (m/seed [{:order-id 1 :asset "BTC" :side :buy}
                     {:order-id 2 :asset "ETH" :side :buy}]))


(def trades (m/seed [{:trade-id 1 :asset "BTC" :side :buy :price 1000.0}
                     {:trade-id 1 :asset "ETH" :side :buy :price 300.0}]))


(def robot (m/ap  (let [order (m/?> orders)
                        _ (println "sending order: " order)
                        trade (m/?> trades)
                        _ (println "received trade: " trade)]
                    [order trade])))


(m/? (m/reduce (fn [_ s]
                 (println "data: " s))
               nil robot))

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



  
  

