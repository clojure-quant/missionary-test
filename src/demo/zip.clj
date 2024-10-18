(ns demo.zip
  (:require
    [missionary.core :as m]))
  


(def a (m/seed (range 3)) )
(def b (m/seed [:a :b :c :d :e :f]))
(def c (m/seed (range 10)))

(def z (m/zip vector a b))


(defn fprint [f]
  (m/?
   (m/reduce (fn [r v]
               (println "v: " v)) nil
        f)))

(fprint z)

(fprint (mix a b))

(fprint (mix a b c))

(fprint (m/zip vector a b c))



(defn forever [task]
  (m/ap (m/? (m/?> (m/seed (repeat task))))))

(defn flow-or-nil [f]
  (let [next-v  ;(m/reduce (fn [r v] v) nil (m/eduction (take 1) f))
                (m/reduce conj [] (m/eduction (take 2) f))
        nil-t (m/sp :ttt)
        value-t (m/race  next-v nil-t
                        )]
    (forever value-t)
        
        )
  
  )

(fprint (m/zip vector (flow-or-nil a) b ))




