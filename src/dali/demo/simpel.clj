(ns dali.demo.simpel
  (:require 
   [missionary.core :as m]
   [dali.dom :as dom]
   [dali.mount :refer [mount unmount]]))


(def body (dom/create-element :div))

body

(def p-hello {:tag :p
              :id 1
              :child-f (m/seed [])})

(def t-bye {:tag :text
            :id 5
            :value-f (m/seed ["bye"])
            :child-f (m/seed [])})

(def p-goodbye {:tag :span
                :id 2
                :child-f (m/seed [[t-bye]])})


(def p-bye {:tag :p
            :id 3
            :child-f (m/seed [[p-goodbye]])})

(def div-main {:tag :div
               :id 4
               :child-f (m/seed [[p-hello] [p-bye]])})

(def system (mount body div-main))
system

(dom/to-hiccup body)
;; => [:div [:div [:p [:span "bye"]]]]

(unmount system)

(dom/to-hiccup body)
;; => [:div]


; (div 
;   (text "hello"))

