(ns demo.batch-v6
   (:require
    [missionary.core :as m]))

;; first test if we can group-by in a completly synchronous way

(def main-f (m/seed [:a :b :c :d :e :f :g ::end]))

(def service-f (m/seed [1 2 3]))

(def work-processor
  (m/ap
   (let [restartable (second (m/?> (m/group-by {} service-f)))]
     (m/? (->> (m/ap (m/amb=
                      (m/?> restartable)
                      (m/?> main-f)))
               (m/eduction (take-while #(not= % ::end)))
               (m/reduce conj))))))

((->> work-processor
      (m/eduction (take 2))
      (m/reduce conj))
 prn prn)

; [[:a 1 :b 2 :c 3 :d :e :f :g]]
;; it worked - we can process multiple seeds and get one big result.


(def r (m/rdv))

;; create a service job that just reads data from 
;; a rendevous and prints it and returns it wrapped in [:service]

(defn action! [a]
  (println "action: " a)
  (m/? (r a))
  (println "action: " a " sent!")
  nil)

(defn forever [task]
  (m/ap (m/? (m/?> (m/seed (repeat task))))))

(def service2-t 
  (m/sp 
   (let [r [:service (m/? r)]]
     (println r)
     r)))

(def service2-f
  (forever service2-t))

;; test the service and send it a few actions

((->> service2-f
      (m/reduce conj))
 prn prn)

(action! :a)
(action! 2)
(action! 5)

;; creaate a job processor, that depending on the job 
;; will have to use the above defined service

(defn process-job [job]
  (case job
    :b (do (action! :BBB) :scheduled-b)
    :d (do (action! :DDD) :scheduled-d)
    :no-action))

(defn create-action-flow [f] 
  (m/ap
   (let [job (m/?> 5 f)]
         (process-job job)       
     )))

;; test the job processor by running it on the main seed

((->> (create-action-flow main-f)
      (m/reduce conj))
 prn prn)

; action:  :DDD
; [:service :DDD]
; action:  :DDD  sent!
; action:  :BBB
; [:service :BBB]
; action:  :BBB  sent!
; [:no-action :scheduled-d :no-action :scheduled-b :no-action :no-action :no-action :no-action]

;; job processor is working


(def work-processor2
  (m/ap
   (let [action-runner (create-action-flow main-f)
         restartable (second (m/?> 100 (m/group-by {} service2-f)))]
      (m/amb=
        (m/?> 100 restartable)
        (m/?> 100 action-runner)))))

((->> work-processor2
      (m/eduction (take 100))
      (m/reduce conj))
 prn prn)


(action! :x)
; I need to call this 2 times before the getting this output:

