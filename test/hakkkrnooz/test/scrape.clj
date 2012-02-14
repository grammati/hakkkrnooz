(ns hakkkrnooz.test.scrape
  (:import (org.jsoup Jsoup)
           (java.io File))
  (:require [hakkkrnooz [scrape :as s]])
  (:use [clojure.test]))

(deftest parse-stories
  (let [f (Jsoup/parse (File. "test/data/main-with-job-ad.html") "utf-8")
        items (s/parse-stories f)
        stories (filter #(= :story (:type %)) items)
        ads (filter #(= :job-ad (:type %)) items)]
    (is (= 30 (count items)))
    (is (= 29 (count stories)))
    (is (= 1 (count ads)))
    ))

