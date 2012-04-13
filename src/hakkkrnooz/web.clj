(ns hakkkrnooz.web
  (:use (ring.adapter [jetty :only  [run-jetty]])
        (compojure [core :only [defroutes GET]])
        (hiccup [core :only [html resolve-uri]]
                [page-helpers :only [html5 include-js include-css link-to]]))
  (:require (compojure [route :as route])
            (hakkkrnooz [data :as data])))


;; serve from files in test/offline instead of scraping the real HN
(def ^:dynamic *offline* false)


(defn include-less [& styles]
  (for [style styles]
    [:link {:type "text/css", :href (resolve-uri style), :rel "stylesheet/less"}]))

(defn- url-for [name]
  ({:jquery (if *offline* "js/jquery.js"
                "http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js")
    :less   (if *offline* "js/less.js"
                "http://cdnjs.cloudflare.com/ajax/libs/less.js/1.1.5/less-1.1.5.min.js")
    :underscore (if *offline* "js/underscore.js"
                    "http://cdnjs.cloudflare.com/ajax/libs/underscore.js/1.3.3/underscore-min.js")
    } name))

(defn story-template []
  [:script {:type "text/template" :id "story-template"}
   [:div.item.story {:id "{{ id }}" :tabindex 1}
    [:a.story-link {:href "{{ href }}"}
     "{{ title }}"]
    [:table.info
     [:tr
      [:td.points "{{ points }} points"]
      [:td.user "by {{ user }}"]
      [:td.cc "comments: {{ cc || 0 }}"]]]]])

(defn comment-template []
  [:script {:type "text/template" :id "comment-template"}
   [:div.item.comment {:id "{{ id }}" :tabindex 1}
    [:table.info
     [:tr
      [:td.user "{{ user }}"]
      [:td.cc "replies: {{ replies.length }}"]]]
    [:div.comment-text
     "{{ comment }}"]]])

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
                "/js/hakkkrnooz.js")]
   [:body.theme-1
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
  (GET "/comments/:id" [id] (comments id)))

(defn -main [port]
  (run-jetty app-routes {:port (Integer. port)}))

