(ns hakkkrnooz.ui.views
    (:require [re-frame.core :as re-frame]))

(def column-width 550)

(defn story [{:keys [id title href points user cc] :as story}]
  (let [focused-item-id (re-frame/subscribe [:focused-item])]
    (fn [{:keys [id title href points user cc] :as story}]
      (println "focused:" @focused-item-id "id:" id)
      [:div.item.story {:id        id
                        :key       id
                        :tabIndex  1
                        :className (when (= id @focused-item-id) "focus" )}
       [:div.story-header
        [:a.story-link {:href href}
         title]]
       [:table.info
        [:tbody
         [:tr
          [:td.points (str points " points")]
          [:td.user (str "by " user)]
          [:td.cc (str "comments: " (or cc 0))]]]]])))

(defn stories-column []
  (let [stories (re-frame/subscribe [:stories])]
    (fn []
      [:div#stories.stories.column {:style {:width column-width}}
       (for [s @stories]
         [story s])])))

(defn main-panel []
  [:div.whitey
   [:div.header
    [:h1 "Hakkkrnooz"]]
   [:div#content.content
    [stories-column]]])
