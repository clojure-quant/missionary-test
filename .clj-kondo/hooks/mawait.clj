(ns hooks.mawait
  (:require [clj-kondo.hooks-api :as h]))

(defn ? [_]
  (let [cs (h/callstack)]
    (when-not (some #(or (= 'sp (:name %))
                         (= 'ap (:name %))) 
                    ; #(and  (= 'clojure.core (:ns %)))
                 cs)
      (throw (ex-info "You should use m/? inside m/sp" {})))))


