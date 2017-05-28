(ns hakkkrnooz.ui.views
    (:require [re-frame.core :as re-frame]))

(def column-width 550)

(defn story [{:keys [id title href points user cc] :as story}]
  [:div.item.story {:id       id
                    :key      id
                    :tabIndex 1}
   [:div.story-header
    [:a.story-link {:href href}
     title]]
   [:table.info
    [:tbody
     [:tr
      [:td.points (str points " points")]
      [:td.user (str "by " user)]
      [:td.cc (str "comments: " (or cc 0))]]]]])

(defn stories-column []
  (let [stories (re-frame/subscribe [:stories])]
    (fn []
      [:div#stories.stories.column {:style {:width column-width}}
       (for [s @stories]
         ^{:key (or (:id s) (:title s))} [story s])])))

(defn main-panel []
  [:div.whitey
   {:on-key-down (fn [evt]
                   (println (.-keyCode evt) evt))}
   [:div.header
    [:h1 "Hakkkrnooz"]]
   [:div#content.content
    [stories-column]]])
