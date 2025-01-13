(ns dali.reactivity
   (:require
    [missionary.core :as m]
    [clojure.walk :refer [prewalk]]
    [clojure.string]))


(defn reactive-symbol? [s]
  (and (symbol? s)
       (clojure.string/ends-with? (name s) "_")))

(some pos? [0 0 0])


(defn reactive-args? [x]
  (println "checking: " x)
  (when (seq? x)
    (let [x-flat (flatten x)]
      (println "seq ok")
      (let [[_f & args] x-flat]
        (when (seq? args)
          (some reactive-symbol? args))))))


(defn reactive-expr [expr]
  (println "reactify: " expr)
  (into [:reactive] expr))

(defn reactify [expr]
  (prewalk
   (fn [x]
     (if (reactive-args? x)
       (reactive-expr x)
       x))
   expr))

(flatten [1 [2 3]])


(reactify '(+ 1 1))

(reactify '(+ 1 asdf_))

(reactify '(+ 1 (* 3 f_)))
;; => [:reactive + 1 [:reactive * 3 f_]]



(reactify
 '(let [a_ 2
        b 3]
    (+ a_ b)))
;; => [:reactive let [a_ 2 b 3] [:reactive + a_ b]]



