(ns dali.dom)

; pElement.textContent = 'hello';

;; IDENTICAL TO DOM

(defn create-element [tag]
  ; document.createElement('div');
  ; document.createTextNode ('hello');
  {:tag tag
   :children (atom [])})

(defn create-text [text]
  {:tag :text
   :children (atom [])
   :value text})

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
    value
    (into [tag]
       (map to-hiccup @children))))