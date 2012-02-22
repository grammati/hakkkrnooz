(ns hakkkrnooz.data
  (:require (hakkkrnooz [scrape :as scrape])
            (clojure.core [memoize :as mem])
            (cheshire [core :as ches]))
  (:import (org.jsoup Jsoup)
           (java.net URL)))


(def HN "http://news.ycombinator.com")

(defn comments-url [id]
  (str HN "/item?id=" id))

(defn stories-url []
  (str HN "/news"))

(defn get-hn-page [url]
  (let [url (if (.startsWith url HN)
              url
              (str HN url))]
    (Jsoup/parse (URL. url) 5000)))

(defn load-stories []
  (-> (stories-url)
      URL.
      (Jsoup/parse 5000)
      scrape/parse-stories))

(defn load-comments
  "Load the HN comments page for the given thread id and scrape the data."
  [id]
  (loop [c nil
         url (comments-url id)]
    (if url
      (let [{:keys [comments more]} (scrape/parse-comments (get-hn-page url))]
        (println "loaded: " url)
        (recur (concat c comments) more))
      c)))


(defn stories
  "Return a data structure representing the top stories on HN."
  []
  ;; TODO - caching
  (load-stories))

(defn comments
  "Return a data structure representing the comments for the HN thread with the given id."
  [id]
  ;; TODO - caching
  (load-comments id))

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



