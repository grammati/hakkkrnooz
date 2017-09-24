(ns hakkkrnooz.ui.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [re-frisk.core :refer [enable-re-frisk!]]
            [hakkkrnooz.ui.subs :as subs]
            [hakkkrnooz.ui.views :as views]
            [hakkkrnooz.ui.config :as config]
            [hakkkrnooz.ui.events :as events]))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (enable-re-frisk!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app"))
  (re-frame/dispatch [:get-stories]))

(defn ^:export init []
  (re-frame/dispatch-sync [:initialize-db])
  (dev-setup)
  (mount-root))

(defn- dummy []
  ;; Trick cider into keeping our requires, for side-effect
  subs/dummy
  config/dummy)