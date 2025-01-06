(ns dali.demo
  (:require 
    [clojure.walk :refer [prewalk]]
   )
  )


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



(dalify 
 '(div (text "test"))
 )

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
                           :children []}]}
              ]})


(render b)