(ns dali.demo.clock
  (:require
   [missionary.core :as m]
   [dali.dom :as dom]
   [dali.mount :refer [mount unmount]]
   [demo.flows.clock :refer [>clock]]))

(def body (dom/create-element :div))

;; counter

(def t-time {:tag :text
             :id 8
             :value-f >clock
             :child-f (m/seed [])})

(def div-main {:tag :div
               :id 4
               :child-f (m/seed [[t-time]])})

(def app (mount body div-main))


(dom/to-hiccup body)
;; => [:div [:div 19]]
;; => [:div [:div 23]]


(unmount app)


(dom/to-hiccup body)
;; => [:div]



