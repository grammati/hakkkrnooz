(ns hakkkrnooz.scrape-test
  (:require [clojure.test :refer :all]
            [hakkkrnooz.scrape :as s]
            [clojure.java.io :as io])
  (:import java.io.File
           org.jsoup.Jsoup))

(deftest parse-stories
  (let [f       (Jsoup/parse (File. "dev-resources/mock-hn/stories") "utf-8")
        items   (s/parse-stories f)
        stories (filter #(= :story (:type %)) items)
        ads     (filter #(= :job-ad (:type %)) items)]
    (is (= 30 (count items)))
    (is (= 29 (count stories)))
    (is (= 1 (count ads)))
    ))

(deftest parse-comments
  (let [{:keys [comments more-link]}
        (-> "dev-resources/mock-hn/comments/14330547"
            io/file
            (Jsoup/parse "utf-8")
            s/parse-comments)]
    (is (nil? more-link))
    (is (= 37 (count comments)))
    (is (= 9 (->> comments first :replies count)))
    (let [{:keys [id user]} (first comments)]
      (is (= "dis-sys" user)))))
