(ns demo.connection.task
  (:require
    [missionary.core :as m]
    [nano-id.core :refer [nano-id]]))


(def connect! 
  (m/sp (let [id (nano-id 5)]
                     (println "connected: " id)
                     id)
                   ))

(def reconnect! 
  (m/sp (m/? (m/sleep 5000))
        (m/? connect!)))



(def poll-conn 
  (m/ap
   (m/amb :init)
   (loop [v (m/? connect!)]
     (m/amb v (recur (m/? reconnect!))))))


(def conn
  (m/signal poll-conn))

(m/? (m/join vector
             (m/reduce conj [] (m/eduction (take 2) conn))
             (m/reduce conj [] (m/eduction (take 1) conn))
             (m/reduce conj [] (m/eduction (take 2) conn))
             (m/reduce conj [] (m/eduction (take 2) conn))
             (m/reduce conj [] (m/eduction (take 2) conn))
             (m/reduce conj [] (m/eduction (take 1) conn))
             (m/reduce conj [] (m/eduction (take 1) conn))
             (m/reduce conj [] (m/eduction (take 1) conn))
             (m/sp
              (m/? (m/sleep 1))
              (m/? (m/reduce conj [] (m/eduction (take 2) conn))))))



[["e6Jou"] ["WSEaF" "Mmhfm"] ["WSEaF" "Mmhfm"]]
