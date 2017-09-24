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
    (spit (str dir "/stories.html") stories-page)
    (doseq [story (->> stories-page
                       Jsoup/parse
                       scrape/parse-stories)]
      (let [id            (:id story)
            comments-page (-> id data/comments-url http/get :body)]
        (try
          (spit (str dir "/comments/" id ".html") comments-page)
          (println "Wrote:" (:title story))
          (catch Exception e
            (println "Error:" (.getMessage e))
            (prn story)))))))

;;; Dev server, for mocking out HN by serving static HTML pages from files
;;; Remember that this handler is called only by server-side code that scrapes
;;; the data from HN pages, and that the URL that the scraper calls to is
;;; controlled by the var `config/HN`, so we just need to alter its root binding
;;; to load our data from local files.

(defn wrap-hn-routing [handler]
  (fn [request]
    ;; This middleware converts real HN-style paths into paths that will match a
    ;; file path in "dev-resources/mock-hn":
    ;;   "/" becomes "/stories.html"
    ;;   "item?id=12345" becomes "/comments/12345.html"
    ;; Then the `route/resources` handler will server the right HTML file.
    (Thread/sleep 2000)
    (println "Mocked HN: " (:uri request))
    (if-let [id (get-in request [:params "id"])]
      (handler (assoc request :uri (str "/comments/" id ".html")))
      (handler (assoc request :uri "/stories.html")))))

(defroutes mock-hn-routes
  (route/resources "/" {:root "mock-hn"})
  (route/not-found "WTF?"))

(def mock-hn-handler
  (-> mock-hn-routes
      wrap-hn-routing
      ring.middleware.params/wrap-params
      prone.middleware/wrap-exceptions))

(defonce servers (atom nil))

(defn run-mock-hn []
  (alter-var-root #'config/HN (constantly "http://localhost:8080"))
  (when-let [server-stop-fn (:hn @servers)]
    (server-stop-fn)
    (swap! servers dissoc :hn ))
  (let [server (ring.adapter.jetty/run-jetty #'mock-hn-handler {:port 8080 :join? false})]
    (swap! servers assoc :hn server)))


;;; Dev helper to run both mock HN and the normal hakkkrnooz server. Don't use
;;; this if you are doing front-end dev - instead, run figwheel as the server,
;;; and call `(run-mock-hn)` in the clojure repl.
(defn run-dev []
  {:hn  (run-mock-hn)
   :web (ring.adapter.jetty/run-jetty #'web/handler {:port 4000 :join? false})})

(defn go []
  (when @servers
    ((:web @servers))
    ((:hn @servers))
    (reset! servers nil))
  (reset! servers (run-dev)))
