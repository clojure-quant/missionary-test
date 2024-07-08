(def hello-world
  (m/ap
    (println (m/?> (m/seed ["Hello" "World" "!"])))
    (m/? (m/sleep 1000))
    1))

(m/? (m/reduce conj hello-world))

Hello
World
!
#_=> [nil nil nil]
