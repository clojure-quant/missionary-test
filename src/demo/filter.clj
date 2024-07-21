(ns demo.filter
  (:require 
    [missionary.core :as m]))




; 'm/eduction with filter 1-arity

(m/? (->> (m/seed (range 20))
        (m/eduction (filter odd?))
        (m/reduce conj)))





(recur (m/amb))






m/amb one arg is identity

