(ns dali.demo
  (:require
   [clojure.walk :refer [prewalk]]
   [clojure.string]))


(def dali-tags #{'div 'p 'text})

(defn dali-fn? [x]
  (when (seq? x)
    (let [[f & _args] x]
      (when (symbol? f)
        (contains? dali-tags f)))))


(defn dali-element [[f & args]]
  (let [a1 (first args)]
    (merge {:et f}
           (if (seq? a1)
             {:value nil
              :children (into [] args)}
             {:value a1
              :children (into [] (rest args))}))))

(defn dalify
  "resolve function-as symbol to function references in the reagent-hickup-map.
   Leaves regular hiccup data unchanged."
  [hiccup-vector]
  (prewalk
   (fn [x]
     (if (dali-fn? x)
       (dali-element x)
       x))
   hiccup-vector))


(+ 1 1)


(dalify
 '(div (text "test")))

(dalify
 '(div (p (text "test"))))
;; => {:et div, :value nil, 
;;       :children 
;;       [{:et p, 
;;         :value nil, 
;;         :children 
;;         [{:et text, :value "test", 
;;           :children 
;;           []}]}]}

(dalify
 '(div (p (text {:class "bold"} "test"))))
;; => {:et div,
;;     :value nil,
;;     :children [{:et p, :value nil, :children [{:et text, :value {:class "bold"}, :children ["test"]}]}]}




(def a
  {:et :div
   :value nil
   :children [{:et :text
               :value "test"
               :children []}]})


(defmulti render :et)


(defmethod render :div [{:keys [et value children]}]
  (into [:div] (map render children)))

(defmethod render :p [{:keys [et value children]}]
  (into [:p] (map render children)))

(defmethod render :text [{:keys [et value children]}]
  value)


(render a)
;; => [:div "test"]

(def b
  {:et :div
   :value nil
   :children [{:et :p
               :value nil
               :children [{:et :text
                           :value "test"
                           :children []}]}]})


(render b)

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


(reactify
 '(let [a_ 2
        b 3]
    (+ a_ b)))
;; => [:reactive let [a_ 2 b 3] [:reactive + a_ b]]



