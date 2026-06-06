(ns bb.demo1
  (:require
   [missionary.core :as m]))


(defn demo1!
  "Connect, submit every order from orderlist/<name>.edn, disconnect, and print
   the submitted orders. `name` is the orderlist file name without extension."
  ([] 
   (println "demo1") 
   (println (m/? (m/sleep 1000 :done)))
   (println "done."))
  )
