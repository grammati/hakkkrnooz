(ns hakkkrnooz.ui.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 :stories
 (fn [db]
   (get-in db [:data :stories])))

(re-frame/reg-sub
 :focused-item
 (fn [db]
   (get-in db [:ui :focused-item])))

(re-frame/reg-sub :ui :ui)

(def dummy 42)
