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
       (do (m/? (m/compel (m/sleep 10000 c)))
           (println "#### re-connecting .. ")
           (recur (nano-id 4))))))))

(m/?
 (m/reduce conj [] 
           (m/eduction (take 2) >conn)))


(m/? (m/join vector
         (m/reduce conj [] (m/eduction (take 1) >conn))
         (m/reduce conj [] (m/eduction (take 2) >conn))
         (m/sp
          (m/? (m/sleep 1))
          (m/? (m/reduce conj [] (m/eduction (take 2) >conn)))))
 )

;; #### creating conn
; #### re-connecting .. 
; [["cK8P" "Qdl3"] ["cK8P" "Qdl3"] ["cK8P" "Qdl3"]]

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



((m/join vector
         (current-v >conn)
         (current-v >conn)
         (m/sp
          (m/? (m/sleep 1))
          (m/? (current-v >conn))
          ))
 prn prn)
