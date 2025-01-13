(ns dali.demo.syntax
  (:require
   [dali.syntax :refer [defc]]
    [dali.dom :as dom]
   [dali.mount :refer [mount unmount]]
   ))


(defc
 '(div (p )))




(def body (dom/create-element :div))

(def app (mount body 
                (defc
                  '(div (p)))
                ))
app

(dom/to-hiccup body)
;; => [:div [:div [:p [:span "bye"]]]]

(unmount app)

(dom/to-hiccup body)

(defc
 '(div (p (text {:class "bold"} "test"))))
;; => {:tag div,
;;     :id 14,
;;     :value-f nil,
;;     :child-f #object[missionary.core$seed$fn__7320 0x7d0f4254 "missionary.core$seed$fn__7320@7d0f4254"]}


(defc
  '(div (p (text "test"))))




