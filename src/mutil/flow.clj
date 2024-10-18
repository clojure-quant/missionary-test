(ns mutil.flow
  (:require
   [missionary.core :as m]))


(defn forever [task]
  (m/ap (m/? (m/?> (m/seed (repeat task))))))

(defn mix
  "Return a flow which is mixed by flows"
  ; will generate (count flows) processes, 
  ; so each mixed flow has its own process
  [& flows]
  (m/ap (m/?> (m/?> (count flows) (m/seed flows)))))

