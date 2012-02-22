(ns hakkkrnooz.web
  (:use (ring.adapter [jetty :only  [run-jetty]])
        (compojure [core :only [defroutes GET]])
        (hiccup [core :only [html resolve-uri]]
                [page-helpers :only [html5 include-js include-css link-to]]))
  (:require (compojure [route :as route])
            (hakkkrnooz [data :as data])))


(defn include-less [& styles]
  (for [style styles]
    [:link {:type "text/css", :href (resolve-uri style), :rel "stylesheet/less"}]))

(defn main-page []
  (html
   {:mode :html}
   [:head
    [:title "Hakkkrnooz"]
    (include-css "/css/reset.css")
    (include-less "/css/hakkkrnooz.less")
    (include-js "http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"
                "http://cdnjs.cloudflare.com/ajax/libs/less.js/1.1.5/less-1.1.5.min.js"
                "/js/hakkkrnooz.js")]
   [:body.theme-1
    [:div.header
     [:h1 "Hakkkrnooz"]]
    [:div.content
     [:div#stories.stories]
     [:div#comments.comments]]]))

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

