(ns demo.connection
  (:require
   [nano-id.core :refer [nano-id]]
   [missionary.core :as m]))

(def >conn
  (m/stream
   (m/ap
    (println "#### creating conn")
    (loop [c (nano-id 4)]
      (m/amb
       c
       (do (m/? (m/sleep 10000 c))
           (println "#### re-connecting .. ")
           (recur (nano-id 4))))))))

(m/?
 (m/reduce conj [] (m/eduction (take 1) >conn)))
;; => ["A-l3"]


(m/?
 (m/reduce conj [] (m/eduction (take 2) >conn)))
;; #### creating conn
;; #### re-connecting .. 
;; => ["x1h_" "El1J"]

((m/join vector
         (m/reduce conj [] (m/eduction (take 2) >conn))
         (m/reduce conj [] (m/eduction (take 2) >conn))
         (m/sp 
            (m/? (m/sleep 1))
            (m/? (m/reduce conj [] (m/eduction (take 2) >conn)))))
 prn prn)
; #### creating conn
; #### re-connecting .. 
; #### re-connecting .. 
; [["V8kV" "vwYI"] ["V8kV" "vwYI"] ["vwYI" "utTA"]]

(def conn2 
 (m/relieve {} >conn) 
  )
 
(m/?
 (m/reduce conj [] (m/eduction (take 2) conn2)))

((m/join vector
         (m/reduce conj [] (m/eduction (take 2) conn2))
         (m/reduce conj [] (m/eduction (take 2) conn2))
         (m/sp
          (m/? (m/sleep 1))
          (m/? (m/reduce conj [] (m/eduction (take 2) conn2)))))
 prn prn)
