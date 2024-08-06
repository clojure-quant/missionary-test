(ns demo.connection.task
  (:require
    [missionary.core :as m]
    [nano-id.core :refer [nano-id]])
  (:import [missionary Cancelled])
  )


(def connect! 
  (m/sp (let [id (nano-id 5)]
          (m/? (m/compel (m/sleep 2000)))
                     (println "connected: " id)
                     id)
                   ))

(def reconnect! 
  (m/sp (m/? (m/sleep 5000))
        (m/? connect!)))


(def shutdown!
  (m/sp 
   (println "shutdown!")
   (m/? (m/sleep 5000))
   ))



(def poll-conn 
  (m/ap
   (try 
     (m/amb "asdf")
   (loop [v (m/? (m/compel connect!))]
     (m/amb v (recur (m/? reconnect!))))  
     (catch Cancelled _ 
       ;(println "shutting down..")
       ;(m/? shutdown!)
       true)
     )
   ))



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

(defn take-first-non-nil [f]
  ; flows dont implement deref
  (m/eduction
   (remove nil?)
   (take 1)
   f))


(defn current-v
  "gets the first non-nil value from the flow"
  [f]
  (m/reduce (fn [_r v]
              (println "current-v: " v)
              v) nil
            (take-first-non-nil f)))


(defn cont
  "converts a discrete flow to a continuous flow. 
    returns nil in the beginning."
  [flow]
  (->> flow
       (m/reductions (fn [r v]
                       (if v v r)) nil)
       (m/relieve {})))


(m/? (current-v (cont poll-conn)))



(def conn
  (m/signal 
  (cont poll-conn) 
   )
  )

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

