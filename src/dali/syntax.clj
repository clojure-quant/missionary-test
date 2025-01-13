(ns dali.syntax
  (:require
   [missionary.core :as m]
   [clojure.walk :refer [prewalk]]
   [clojure.string]))

(defonce max-id (atom 0))

(defn create-id []
  (swap! max-id inc))

(def dali-tags #{'div 'p 'text})

(defn dali-fn? [x]
  (when (seq? x)
    (let [[f & _args] x]
      (when (symbol? f)
        (contains? dali-tags f)))))

(defn dali-element [[f & args]]
  (let [a1 (first args)]
    (merge {:tag f
            :id (create-id)
            }
           (if (seq? a1)
             {:value-f nil
              :child-f (m/seed [(into [] args)])}
             {:value-f (m/seed [a1])
              :child-f (m/seed [(into [] (rest args))])}))))



(defn defc
  [hiccup-vector]
  (prewalk
   (fn [x]
     (if (dali-fn? x)
       (dali-element x)
       x))
   hiccup-vector))