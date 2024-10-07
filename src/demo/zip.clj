(ns demo.zip
  (:require
    [missionary.core :as m]))
  
(defn mix
  "Return a flow which is mixed by flows"
  ; will generate (count flows) processes, 
  ; so each mixed flow has its own process
  [& flows]
  (m/ap (m/?> (m/?> (count flows) (m/seed flows)))))

(def a (m/seed (range 3)) )
(def b (m/seed [:a :b :c :d :e :f]))

(def z (m/zip vector a b))


(defn fprint [f]
  (m/?
   (m/reduce (fn [r v]
               (println "v: " v)) nil
        f)))

(fprint z)

(fprint (mix a b))


