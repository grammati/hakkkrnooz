(ns hakkkrnooz.ui.views
  (:require [hakkkrnooz.ui.keys :as keys]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]))

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
    (reagent/create-class
     {:component-did-mount
      #(re-frame/dispatch [:focus-first])
      :reagent-render
      (fn []
        [:div#stories.stories.column {:style {:width column-width}}
         (for [s @stories]
           ^{:key (or (:id s) (:title s))} [story s])])} )))

(defn main-panel []
  (let [ready? (re-frame/subscribe [:ready?])]
    (fn []
      [:div.whitey
       {:on-key-down keys/handle-keydown}
       [:div.header
        [:h1 "Hakkkrnooz"]]
       [:div#content.content
        (if (true? @ready?)
          [stories-column]
          [:div.column [:h1 "loading..."]])]])))
