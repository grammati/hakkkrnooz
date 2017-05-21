(ns dev
  (:require [clj-http.client :as http]
            [clojure.java.shell :as sh]
            [clojure.string :as str]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [hakkkrnooz.config :as config]
            [hakkkrnooz.data :as data]
            [hakkkrnooz.scrape :as scrape]
            [hakkkrnooz.web :as web]
            ring.adapter.jetty
            ring.middleware.params)
  (:import org.jsoup.Jsoup))

(defn rmr-dir [dir]
  (let [path (-> dir java.io.File. .getCanonicalPath)]
    (assert (and (string? path) (<= 6 (-> path (str/split #"/") count)))
            (str "Path too short, I refuse to rm -rf it! path: " path))
    (sh/sh "rm" "-rf" path)
    (println "Deleted: " path)))

(defn ensure-dir [dir]
  (-> dir java.io.File. .mkdir))

(defn dump-raw
  "Grab a dump of the current HN front page, including all comments, and put
  them into HTML files under test/data."
  []
  (let [dir          "dev-resources/mock-hn"
        stories-page (-> (data/stories-url) http/get :body)]
    (rmr-dir dir)
    (ensure-dir dir)
    (ensure-dir (str dir "/comments"))
    (spit (str dir "/stories") stories-page)
    (doseq [story (->> stories-page
                       Jsoup/parse
                       scrape/parse-stories)]
      (let [id            (:id story)
            comments-page (-> id data/comments-url http/get :body)]
        (try
          (spit (str dir "/comments/" id) comments-page)
          (println "Wrote:" (:title story))
          (catch Exception e
            (println "Error:" (.getMessage e))
            (prn story)))))))

(defn wrap-hn-routing [handler]
  (fn [request]
    (prn request)
    (if-let [id (get-in request [:params "id"])]
      (handler (assoc request :uri (str "/comments/" id)))
      (handler (assoc request :uri "/stories")))))

(defroutes mock-hn-routes
  (route/resources "/" {:root "mock-hn"})
  (route/not-found "WTF?"))

(def mock-hn-handler
  (-> mock-hn-routes
      wrap-hn-routing
      ring.middleware.params/wrap-params
      prone.middleware/wrap-exceptions))

(defn run-dev []
  (alter-var-root #'config/HN (constantly "http://localhost:8080"))
  {:hn  (ring.adapter.jetty/run-jetty #'mock-hn-handler {:port 8080 :join? false})
   :web (ring.adapter.jetty/run-jetty #'web/handler {:port 4000 :join? false})})

(defonce servers (atom nil))

(defn go []
  (when @servers
    ((:web @servers))
    ((:hn @servers))
    (reset! servers nil))
  (reset! servers (run-dev)))
