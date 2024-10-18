(ns demo.flows.timebuffered
  (:require
    [missionary.core :as m]))

(defn time-buffered [duration-ms flow]
  (m/ap
   (let [restartable (second (m/?> (m/group-by {} flow)))]
     (m/? (->> (m/ap (m/amb= (m/?> restartable)
                             (m/? (m/sleep duration-ms ::end))))
               (m/eduction (take-while #(not= % ::end)))
               (m/reduce conj))))))
