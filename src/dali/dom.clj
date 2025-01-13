(ns dali.dom)

;; IDENTICAL TO DOM

(defn create-element [tag]
  ; document.createElement('div');
  {:tag tag
   :children (atom [])})

(defn create-text []
  ; document.createTextNode ('hello');
  {:tag :text
   :children (atom [])
   :value (atom "")})

(defn change-text [el text]
  ; textNode.textContent = 'World';
  (reset! (:value el) text))


(defn append-child [parent-el child-el]
  ; document.body.appendChild(divElement);
  (println "dom/append-child")
  (swap! (:children parent-el) conj child-el))

(defn remove-child [parent-el child-el]
  ;parentElement.removeChild (childElement);
  (println "dom/remove-child")
  (let [ex-child (->> @(:children parent-el)
                      (remove #(= % child-el))
                      (into []))]
    (reset! (:children parent-el) ex-child)))


(defn to-hiccup [{:keys [tag children value]}]
  (if (= tag :text)
    @value
    (into [tag]
       (map to-hiccup @children))))