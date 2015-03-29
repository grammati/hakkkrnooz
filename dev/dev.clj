(ns dev
  (:require [cheshire.core :as json]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [hakkkrnooz.data :as data]
            [hakkkrnooz.web :as web]
            [ring.adapter.jetty :refer [run-jetty]]))


(defn ensure-dir [dir]
  (-> dir java.io.File. .mkdir))

(defn dump
  "Grab a dump of the current HN front page, including all comments,
  and put them into files in resources/data."
  []
  (let [dir     "resources/data"
        stories (data/load-stories)]
    (ensure-dir dir)
    (ensure-dir (str dir "/comments"))
    (spit (str dir "/stories") (json/encode stories))
    (doseq [story stories :let [id (:id story)] :when id]
      (let [comments (data/load-comments id)]
        (spit (str dir "/comments/" id) (json/encode comments))
        (println "Wrote:" (:title story))))))

(defroutes dev-routes
  (GET "/" [] (web/main-page))
  (route/resources "/")
  (route/resources "/" {:root "data"}))

(defn run-dev []
  (run-jetty #'dev-routes {:port 8080 :join? false}))
