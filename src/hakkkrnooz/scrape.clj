(ns hakkkrnooz.scrape
  (:require [clojure.string :as s]
            [hakkkrnooz.config :refer [HN]]
            [clojure.string :as str])
  (:import clojure.lang.ExceptionInfo
           [org.jsoup.nodes Document Element TextNode]
           [org.jsoup.select Elements Selector]))

(set! *warn-on-reflection* true)

;; Protocol - exists so that Jsoup's Selector/select can be used
;; without reflection.
(defprotocol Searchable
  ($ ^Elements [e q]))

(extend-protocol Searchable
  Element
  ($ ^Elements [e ^String q] (Selector/select q e))
  Iterable
  ($ ^Elements [e ^String q] (Selector/select q e))
  nil
  ($ [_ _] nil))

(defn $1 ^Element [elt selector]
  (.first ($ elt selector)))

(defn block? [n]
  (and (instance? Element n)
       (.isBlock ^Element n)))

(defn inline? [n]
  (not (block? n)))

(defn text [^Element node]
  (.text node))

(defn attr ^String [^Element node ^String attr-name]
  (.attr node attr-name))

(defn- empty-text-node? [node]
  (and (instance? TextNode node) (empty? (s/trim (.text ^TextNode node)))))

(defn comment-text [^Element span]
  ;; see the explanation of parse-comment, below.
  ;; -span
  ;;   - text node
  ;;   - p tags for additional paragraphs
  ;;   - reply-link
  (when span
    (let [items (remove empty-text-node? (.childNodes span))]
      (loop [[item & items :as all] (butlast items)
             blocks []]
        (if-not item
          blocks
          (if (block? item)
            (recur items (conj blocks (str item)))
            (let [[inl more] (split-with inline? all)]
              (recur more (conj blocks (str "<p>" (apply str inl) "</p>"))))))))))

(defn parse-comment
  "Parse the data out of the markup for a single comment on HN."
  [^Element tr]
  ;; The markup on the HN comments page is rather bizarre, but easy
  ;; enough to extract the data from.
  ;; tr is a row with 3 cells
  ;;  - the first is a spacer, where the width of an image determines the nesting
  ;;  - the second contains the upvote button w
  ;;  - the third contains everything else - specifically, it contains:
  ;;    - first, a div containing a span containing either:
  ;;      - all three of:
  ;;        - the author's handle, and
  ;;        - the text "<N> hours ago |", and
  ;;        - a link (which gives us the comment-id)
  ;;      - or - nothing, if the comment has been deleted
  ;;    - then there is a br (which seems to compensate for the -10px
  ;;      bottom-margin on the div above :/)
  ;;    - then, there is a div with class "comment" containing a single span,
  ;;      which contains:
  ;;      - some text (the first, and possibly only, paragraph)
  ;;      - For a multi-paragraph comment, one or more additional p tags
  ;;      - the reply link (as div with class "reply")
  (let [depth (-> tr
                  ($1 "> td:eq(0) > img")
                  (.attr "width")
                  Long/valueOf)
        header ($1 tr "> td:eq(2) > div:eq(0) > span")
        del    (not (.hasText header))
        user   (if-not del
                 (-> header
                     ($1 "> a:eq(0)")
                     .text))
        age    nil                      ;FIXME
        id     (if-not del
                 (-> header
                     ($ "a")
                     (->> (map #(attr % "href"))
                          (filter #(.startsWith % "item?id=")))
                     first
                     (or "xxxxxxxx")
                     (subs #=(count "item?id="))))
        main   ($1 tr "> td:eq(2) > div.comment > span")
        paras  (comment-text main)
        full-comment (apply str paras)]
    {:type    :comment
     :depth   depth
     :user    user
     :id      id
     :comment full-comment}))

(defn nest-comments [cs]
  (loop [[c & cs] (seq cs)
         tlc []]                        ;top-level-comments
    (if (nil? c)
      tlc
      (let [[desc others] (split-with #(> (:depth %) (:depth c)) cs)
            c (-> c
                  (assoc :replies (nest-comments desc))
                  (dissoc :depth))]
        (recur others (conj tlc c))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; debugging stuff

(defn left [^String s n]
  (.substring s 0 (min n (count s))))

(defn print-summary
  "for testing that I got it right"
  ([cs] (print-summary cs 40))
  ([cs n]
     (letfn [(p [c ind]
               (println (apply str (repeat ind "  "))
                        (:user c) " -> " (left (:comment c) n) "...")
               (doseq [c (:replies c)]
                 (p c (inc ind))))]
       (doseq [c cs] (p c 0)))))

(defmacro tap [x]
  `(let [tap# ~x]
     (println "tap: " '~x " : " tap#)
     tap#))

(defmacro -tap->
  "Debug that shit"
  ([x] x)
  ([x form]
     (if (seq? form)
       (list `tap (with-meta `(~(first form) ~x ~@(next form)) (meta form)))
       `(tap (~form ~x))))
  ([x form & more]
     `(-tap-> (-tap-> ~x ~form) ~@more)))

;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn- main-section
  [^Document doc]
  (-> doc
      .body
      ;; There are four "top-level" parts (tr's): toolbar, spacer,
      ;; main-section, footer. Extract them and just keep the third.
      ($ "> center > table > tbody > tr")
      (.get 2)))

(defn parse-story
  "Given the two tr elements that make up a story on HN, returns the data."
  [title-row info-row]
  (let [link ($1 title-row "> td.title > a")
        info ($1 info-row "> td.subtext")
        info (if info (.childNodes info))
        info (remove empty-text-node? info)
        href (.attr link "href")
        href (if (.contains href "://") ; not sure if there's a more robust what to detect non-abs urls
               href
               (str HN "/" href))
        item {:title (.text link)
              :href  href}]
    (case (count info)
      ;; Job add: [<span>3 hours ago</span>, "|", <a ...>hide</a>]
      3 (assoc item
               :type :job-ad
               :age (-> info first text s/trim))
      ;; Story: [points, "by", user, age, empty-span, "|", hide-link, "|", comments-link]
      9 (let [[^Element pt-span
               _
               ^Element user
               ^Element age
               _ _ _ _
               ^Element comment-link] info]
          (assoc item
                 :type :story
                 :points (->> pt-span .text (re-find #"(\d+) points") second)
                 :user (.text user)
                 :age (->> age .text s/trim )
                 :id (->> (.attr comment-link "href") (re-matches #"item\?id=(\d+)") second)
                 :cc (->> comment-link .text (re-matches #"(\d+).+comments") second)))
      (throw (ex-info "Could not parse story"
                      {:title-row      title-row
                       :info-row       info-row
                       :extracted-info info})))))

(defn try-parse-story [title-row info-row]
  (try
    (parse-story title-row info-row)
    (catch ExceptionInfo e
      (assoc (ex-data e) :type :parse-error))))

(defn parse-stories
  "Extract the stories from the HN main page."
  [^Document doc]
  (let [trs (-> doc
                main-section
                ;; On this page, the main section contains a table in which each
                ;; group of 3 rows is a story.
                ($ "> td > table > tbody > tr"))]
    (for [[title-row info-row _] (partition 3 trs)]
      (try-parse-story title-row info-row))))

(defn parse-comments
  "Extract the comments from an HN comment page."
  [^Document doc]
  (let [trs (-> doc
                main-section
                ;; On the main comments page, the main tr further contains two
                ;; tables: the new-comment-form, and the comments. However, on
                ;; page 2+ of comments, the new-comment form is not there. So to
                ;; get the comments, we just take the last table.
                ($ "> td > table")
                last
                ;; Each row of this table contains a single comment, except
                ;; possibly the last, which may contain a "More" link.
                ($ "> tbody > tr"))
        ;; Each comment-row single comment, wrapped in yet-another table, which
        ;; itself has only one row.
        comment-rows ($ trs " > td > table > tbody > tr")
        ;; And find the "More" link, if present.
        more-link (let [link ($ (last trs) "> td > a")]
                    (if (and link (= "More" (.text link)))
                      (.attr link "href")))
        comments (nest-comments (map parse-comment comment-rows))
        ]
    {:comments comments
     :more more-link}))
