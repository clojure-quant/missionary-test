(ns dali.mount
  (:require 
   [clojure.set :as set]
   [missionary.core :as m]
   [dali.dom :as dom]))

(defonce max-id (atom 0))

(defn create-id []
  (swap! max-id inc))

(declare mount)
(declare unmount)

(defn sync-children [{:keys [this-el childrens]} current-children]
  (let [existing-ids (->> @childrens keys (into #{}))
        current-ids (->> current-children (map :id) (into #{}))
        to-add (set/difference current-ids existing-ids) ; Elements in current but not in existing
        to-remove (set/difference existing-ids current-ids)  ; Elements in existing but not in current
]
    (println "children existing: " existing-ids " add: " to-add " remove: " to-remove)
    (doall (for [id-remove to-remove]
             (do (println "removing id: " id-remove)
                 (let [child-remove (get @childrens id-remove)]
                    (unmount child-remove)
                    (swap! childrens dissoc id-remove)))))
    (doall (for [child current-children]
      (let [child-id (:id child)] 
         (if (contains? to-add child-id)
           (do 
              (println "mounting child:" child " to parent: " this-el)
             (let [child (mount this-el child)]
               (swap! childrens assoc child-id child)))
           (println "child already existing: " child))
         )))))

(defn mount [parent-el {:keys [tag id value child-f]}]
  (println "mounting: " tag " " id)
  (let [this-el (if (= tag :text)
                  (dom/create-text value)
                  (dom/create-element tag))
        this  {:parent-el parent-el
               :id id
               :tag tag
               :value value
               :this-el this-el
               :childrens (atom {})}
        child-update-t (m/reduce 
                         (fn [_r children]
                           (println "children update: " children)
                           (sync-children this children)
                           nil)
                         nil child-f)]
    (dom/append-child parent-el this-el)
    (assoc this 
            :child-t (child-update-t
                     #(println "mount children task completed: " %)
                     #(println "mount children task crashed: " %)))))

(defn unmount [{:keys [parent-el id tag this-el child-t childrens] :as opts}]
  (println "unmounting opts: " opts)
  (println "unmounting: " tag " " id " parent-el: " parent-el)
  (if child-t 
     (child-t) ; stop the child-task  
     (println "error: child-task is nil"))
  (doall (map unmount (vals @childrens)))
  (dom/remove-child parent-el this-el))


(def body (dom/create-element :div))

body

(def p-hello {:tag :p
              :id 1
              :child-f (m/seed [])})

(def t-bye {:tag :text
            :id 5
            :value "bye"
            :child-f (m/seed [])})

(def p-goodbye {:tag :span
                :id 2
                :child-f (m/seed [[t-bye]])})


(def p-bye {:tag :p
            :id 3
            :child-f (m/seed [[p-goodbye]])})

(def div-main {:tag :div
               :id 4
               :child-f (m/seed [[p-hello] [p-bye]])
               })



(def system (mount body div-main))
system

(dom/to-hiccup body)
;; => [:div [:div [:p [:span]]]]

(unmount system)

(dom/to-hiccup body)