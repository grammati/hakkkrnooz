(ns hakkkrnooz.web
  (:use (ring.adapter [jetty :only  [run-jetty]])
        (compojure [core :only [defroutes GET]])
        (hiccup [core :only [html]]
                [page-helpers :only [include-js include-css link-to]]))
  (:require (compojure [route :as route])
            (hakkkrnooz [data :as data])))

(defn main-page []
  (html
   {:mode :html}
   [:head
    [:title "Hakkkrnooz"]
    (include-css "/css/reset.css"
                 "/css/hakkkrnooz.css")
    (include-js "http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"
                "/js/hakkkrnooz.js")]
   [:body.theme-1
    [:div.header
     [:h1 "Hakkkrnooz"]]
    [:div.content
     (link-to "/comments/3579847" "/comments/3579847")
     [:div#comments.comments]]]))

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
  (GET "/comments/:id" [id] (comments id)))

(defn -main [port]
  (run-jetty app-routes {:port (Integer. port)}))

