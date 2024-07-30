(ns demo.connection.signal
  (:require
   [nano-id.core :refer [nano-id]]
   [missionary.core :as m]))

(def >conn
  (m/signal
   (m/ap
    (println "#### creating conn")
    (loop [c (nano-id 4)]
      (m/amb
       c
       (do (m/? (m/sleep 10000 c))
           (println "#### re-connecting .. ")
           (recur (nano-id 4))))))))

(m/?
 (m/reduce conj [] 
           (m/eduction (take 2) >conn)))


((m/join vector
         (m/reduce conj [] (m/eduction (take 2) >conn))
         (m/reduce conj [] (m/eduction (take 2) >conn))
         (m/sp
          (m/? (m/sleep 1))
          (m/? (m/reduce conj [] (m/eduction (take 2) >conn)))))
 prn prn)
;; #### creating conn
; #### re-connecting .. 
; [["cK8P" "Qdl3"] ["cK8P" "Qdl3"] ["cK8P" "Qdl3"]]
