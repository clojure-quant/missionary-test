(ns dali.mount
  (:require 
   [clojure.set :as set]
   [missionary.core :as m]
   [dali.dom :as dom]))

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

(defn mount [parent-el {:keys [tag id value-f child-f]}]
  (println "mounting: " tag " " id)
  (let [this-el (if (= tag :text)
                  (dom/create-text)
                  (dom/create-element tag))
        this  {:parent-el parent-el
               :id id
               :tag tag
               :this-el this-el
               :childrens (atom {})}
        child-update-t (m/reduce 
                         (fn [_r children]
                           (println "children update: " children)
                           (sync-children this children)
                           nil)
                         nil child-f)
        value-update-t (when value-f 
                  (m/reduce
                     (fn [_r text]
                          (println "text update: " text)
                          (dom/change-text this-el text)
                          nil)
                        nil value-f))]
    (dom/append-child parent-el this-el)
    (assoc this 
            :child-t (child-update-t
                     #(println "mount children task completed: " %)
                     #(println "mount children task crashed: " %))
            :value-t (when value-update-t
                       (value-update-t
                        #(println "value-update task completed: " %)
                        #(println "value-update task crashed: " %))
                       )
           )))

(defn unmount [{:keys [parent-el id tag this-el child-t value-t childrens] :as opts}]
  (println "unmounting opts: " opts)
  (println "unmounting: " tag " " id " parent-el: " parent-el)
  (if child-t 
     (child-t) ; stop the child-task  
     (println "error: child-task is nil"))
  (if value-t
    (value-t) ; stop the value-task  
    (println "error: value-task is nil"))
  (doall (map unmount (vals @childrens)))
  (dom/remove-child parent-el this-el))