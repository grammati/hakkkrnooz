(ns hakkkrnooz.web
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [hakkkrnooz.data :as data]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [prone.middleware]
            ring.adapter.jetty))


(defn include-less [& styles]
  (for [style styles]
    [:link {:type "text/css", :href style, :rel "stylesheet/less"}]))

(defn- url-for [name]
  ({:jquery     "https://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js"
    :less       "https://cdnjs.cloudflare.com/ajax/libs/less.js/2.7.2/less.min.js"
    :underscore "https://cdnjs.cloudflare.com/ajax/libs/underscore.js/1.8.3/underscore-min.js"
    } name))

(defn story-template []
  [:script {:type "text/template" :id "story-template"}
   [:div.item.story {:id "{{ s.id }}" :tabindex 1}
    [:div.story-header
     [:a.story-link {:href "{{ s.href }}"}
      "{{ s.title }}"]]
    [:table.info
     [:tr
      [:td.points "{{ s.points }} points"]
      [:td.user "by {{ s.user }}"]
      [:td.cc "comments: {{ s.cc || 0 }}"]]]]])

(defn comment-template []
  [:script {:type "text/template" :id "comment-template"}
   [:div.item.comment {:id "{{ c.id }}" :tabindex 1}
    [:div.comment-inner
     [:table.info
      [:tr
       [:td.user "{{ c.user }}"]
       [:td.cc "replies: {{ c.replies.length }}"]]]
     [:div.comment-text
      "{{ c.comment }}"]]]])

(defn main-page []
  (html
   {:mode :html}
   [:head
    [:title "Hakkkrnooz"]
    (include-css "/css/reset.css")
    (include-less "/css/hakkkrnooz.less")
    (story-template)
    (comment-template)
    (include-js (url-for :jquery)
                (url-for :underscore)
                (url-for :less)
                "/js/jquery.cookie.js"
                "/js/hakkkrnooz.js")]
   [:body.whitey
    [:div.header
     [:h1 "Hakkkrnooz"]]
    [:div#content.content
     [:div#stories.stories.column]]]))

(defn stories []
  (data/stories-json))

(defn comments [id]
  (if-let [resp (data/comments-json id)]
    {:status 200
     :headers {"Content-Type" "application/json"
               ;"Content-Encoding" "gzip" 
               }
     :body resp
     }
    {:status 404
     :headers {}
     :body "{\"error\":\"not found\"}"
     }))

(defroutes app-routes
  (GET "/" [] (main-page))
  (route/resources "/")
  (GET "/stories" [] (stories))
  (GET "/comments/:id" [id] (comments id))
  (route/not-found "<h1>¯\\_(ツ)_/¯</h1>"))

(def handler
  (-> app-routes
      ;prone.middleware/wrap-exceptions
      ))

(defn start
  ([]
   (start 4000))
  ([port]
   (ring.adapter.jetty/run-jetty #'handler {:port (Integer. port)})))
