(ns hakkkrnooz.data
  (:require (hakkkrnooz [scrape :as scrape])
            (cheshire [core :as ches]))
  (:import (org.jsoup Jsoup)
           (java.net URL)))


(def HN "http://news.ycombinator.com")

(defn comments-url [id]
  (str HN "/item?id=" id))

(defn stories-url []
  (str HN "/news"))

(defn load-and-soupify [url]
  (Jsoup/parse (URL. url) 5000))

(defn load-stories []
  (-> (stories-url)
      load-and-soupify
      scrape/parse-stories))

(defn load-comments
  "Load the HN comments page for the given thread id and scrape the data."
  [id]
  (loop [c nil
         url (comments-url id)]
    (if url
      (let [{:keys [comments more]} (scrape/parse-comments (load-and-soupify url))]
        (println "loaded: " url)
        (recur (concat c comments) more))
      c)))


(def cache (atom nil))
(def cache-ttl (* 1000 60 3))

(defn get-cached* [key delayed-value & [force]]
  @(locking cache
     (let [{:keys [t v]} (get @cache key)]
       (if (and v (not force) (< (System/currentTimeMillis) (+ t cache-ttl)))
         (do (println "Returning cached value for key " key) v)
         (-> cache
             (swap! assoc key {:t (System/currentTimeMillis)
                               :v delayed-value})
             (get key)
             (get :v))))))

(defmacro get-cached [key & body]
  `(get-cached* ~key (delay ~@body)))

(defmacro force-get [key & body]
  `(get-cached* ~key (delay ~@body) true))


(defn stories
  "Return a data structure representing the top stories on HN."
  []
  (get-cached 0 (load-stories)))

(defn comments
  "Return a data structure representing the comments for the HN thread with the given id."
  [id]
  (get-cached id (load-comments id)))

(defn to-json [o]
  (ches/generate-string o))

(defn stories-json []
  (to-json (stories)))

(defn comments-json [id]
  (to-json (comments id)))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; This doesn't friggin' work
(defn gzipped-json [obj]
  (let [baos (java.io.ByteArrayOutputStream.)
        gzos (java.util.zip.GZIPOutputStream. baos)
        osw  (java.io.OutputStreamWriter. gzos "UTF-8")
        bw   (java.io.BufferedWriter. osw)]
    (cheshire.core/generate-stream obj bw)
    (.flush bw)
    (.flush osw)
    (.flush gzos)
    (java.io.ByteArrayInputStream. (.toByteArray baos))))



