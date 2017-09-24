(ns hakkkrnooz.ui.events
  (:require [ajax.core :as ajax]
            [hakkkrnooz.ui.db :as db]
            [re-frame.core :as re-frame]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
 :get-stories
 (fn [db [id data]]
   (ajax/GET "/stories"
             {:response-format :json
              :keywords? true
              :handler (fn [stories]
                         (println "GOT STORIES!!!!")
                         (re-frame/dispatch [:stories-received stories]))})
   (assoc-in db [:ui :stories :loading?] true)))

(re-frame/reg-event-db
 :focus-item
 (fn [db [_ item]]
   (if-let [elt (.getElementById js/document (:id item))]
     (do
       (println "found: " elt)
       (.focus elt))
     (println "not found: " (:id item)))
   (assoc-in db [:ui :focused-item] (:id item))))

(re-frame/reg-event-db
 :focus-first
 (fn [db _]
   (re-frame/dispatch [:focus-item (-> db :data :stories first)])
   db))

(re-frame/reg-event-db
 :stories-received
 (fn [db [_ stories]]
   (-> db
       (assoc-in [:data :stories] stories)
       (assoc :ready? true))))
