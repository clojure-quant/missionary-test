(ns demo.groupby)


(def words ["Air" "Bud" "Cup" "Awake" "Break" "Chunk" "Ant" "Big" "Check"])
(def groups
  (m/ap (let [[k >x] (m/?> ##Inf (m/group-by (juxt first count) (m/seed words)))]
          [k (m/? (m/reduce conj >x))])))

(m/? (m/reduce conj {} groups))