(ns demo.batch-v3
  (:require
   [missionary.core :as m]
   [demo.flows.producer :refer [data-producer]]))

(defn mix [& flows] (m/ap (m/?> (m/?> (count flows) (m/seed flows)))))

(defn time-buffered [duration-ms flow]
  (m/ap
   (let [restartable (second (m/?> (m/group-by {} flow)))]
     (m/? (->> (m/ap (m/amb= (m/?> restartable)
                             (m/? (m/sleep duration-ms ::end))))
               (m/eduction (take-while #(not= % ::end)))
               (m/reduce conj))))))


(def producer-slow (data-producer 200))
(def producer-fast (data-producer 30))

;; consume batch of values every 50 ms

((->> (time-buffered 50 producer-fast)
      (m/eduction (take 20))
      (m/reduce conj))
 prn prn)
;; => #object[missionary.impl.Reduce$Process 0x62ed649a "missionary.impl.Reduce$Process@62ed649a"]
; [[0 1 2 3] [4 5] [6 7 8 9 10] [11 12 13 14] [15 16 17] [18 19 20]
; [21 22 23 24 25 26] [27 28 29 30] [31 32 33 34] [35 36 37 38]
; [39 40 41 42 43 44 45] [46 47 48 49] [50 51 52 53] [54 55 56] [57 58 59 60 61 62] 
; [63 64 65 66] [67 68 69 70 71] [72 73 74] [75 76 77 78] [79 80 81 82 83]]

((->> (time-buffered 50 (mix producer-slow producer-fast))
      (m/eduction (take 20))
      (m/reduce conj))
 prn prn)