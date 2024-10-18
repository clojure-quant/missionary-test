(ns demo.flow-transform.debounce
  (:require [missionary.core :as m])
  (:import missionary.Cancelled))


(defn debounce [delay flow]
  (m/ap (let [x (m/?< flow)]
          (try (m/? (m/sleep delay x))
               (catch Cancelled _ (m/amb))))))

(m/? (->> (m/ap (let [n (m/amb 24 79 67 34 18 9 99 37)]
                  (m/? (m/sleep n n))))
          (debounce 50)
          (m/reduce conj)))
;; => [24 79 9 37]



;; debounce allows a value to go through if before it 
;; there was a certain pause of no events for some time.