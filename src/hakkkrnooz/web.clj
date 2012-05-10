(ns hakkkrnooz.web
  (:use (ring.adapter [jetty :only  [run-jetty]])
        (compojure [core :only [defroutes GET]])
        (hiccup [core :only [html]]
                [util :only [to-uri]]
                [element :only [link-to]]
                [page :only [html5 include-js include-css]]))
  (:require (compojure [route :as route])
            (hakkkrnooz [data :as data])))


;; serve from files in test/offline instead of scraping the real HN
(def ^:dynamic *offline* false)


(defn include-less [& styles]
  (for [style styles]
    [:link {:type "text/css", :href (to-uri style), :rel "stylesheet/less"}]))

(defn- url-for [name]
  ({:jquery (if *offline* "js/jquery.js"
                "http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js")
    :less   (if *offline* "js/less.js"
                "http://cdnjs.cloudflare.com/ajax/libs/less.js/1.1.5/less-1.1.5.min.js")
    :underscore (if *offline* "js/underscore.js"
                    "http://cdnjs.cloudflare.com/ajax/libs/underscore.js/1.3.3/underscore-min.js")
    } name))

(defn column-template []
  [:script {:type "text/template" :id "column-template"}
   [:div.column
    [:div.column-inner]]])

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
    (column-template)
    (story-template)
    (comment-template)
    (include-js (url-for :jquery)
                (url-for :underscore)
                (url-for :less)
                "/js/jquery.cookie.js"
                "/js/hakkkrnooz.js")]
   [:body
    [:div.header
     [:h1 "Hakkkrnooz"]]
    [:div#content.content]]))

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
  (GET "/comments/:id" [id] (comments id)))

(defn -main [port]
  (run-jetty app-routes {:port (Integer. port)}))

