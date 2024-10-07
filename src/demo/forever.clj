(ns demo.forever
  (:require
    [missionary.core :as m]))


(defn forever [task]
  (m/ap (m/? (m/?> (m/seed (repeat task))))))