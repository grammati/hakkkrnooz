(ns hakkkrnooz.core
  (:import (org.jsoup Jsoup)
           (org.jsoup.nodes TextNode Element)
           (org.jsoup.select Selector)
           (java.net URL)))

(set! *warn-on-reflection* true)

(defn $ [elt ^String selector]
  (Selector/select selector elt))

(defn $1 [elt selector]
  (first ($ elt selector)))

(def HN "http://news.ycombinator.com")

(defn comment-page-url [id]
  (str HN "/item?id=" id))

(defn all-text [e]
  (->> e
      .getAllElements
      (map #(.ownText %))
      (filter #(pos? (.length %)))))

(defn parse-comment
  "Parse the data out of the markup for a single comment on HN."
  [tr]
  ;; The markup on the HN comments page is rather bizarre, but easy
  ;; enough to extract the data from.
  ;; tr is a row with 3 cells
  ;;  - the first is a spacer, where the width of an image determines the nesting
  ;;  - the second contains the upvote button
  ;;  - the third contains everyhting else - specifically, this td contains:
  ;;    - first, a div containing:
  ;;      - the author's handle
  ;;      - the text "<N> hours ago |"
  ;;      - a link (which gives us the comment-id)
  ;;    - then there is a br (which seems to compentate for the -10px
  ;;      bottom-margin on the div above :/)
  ;;    - then, there are two possibilities:
  ;;      1) For a single-paragraph comment, there is a span containing a font
  ;;         tag (really) that contains the text of the comment, and then there
  ;;         is a p tag containing the reply link.
  ;;      2) For a multi-paragraph comment, the last paragraph seems to get spit
  ;;         out of the end of the font tag to become a child of the span, and
  ;;         the reply-link gets sucked into the span. Someone seems to have
  ;;         worked around the first of these oddities by wrapping another font
  ;;         tag around the text of the spat-out paragrapsh to get the color
  ;;         right. Furthermore, the first paragraph under the font tag is not
  ;;         wrapped in a p tag, but all the rest are. And yes, in case you are
  ;;         wondering, the span that contains all these paragraphs is an inline
  ;;         element, and it's full of block elements. Good thing browsers are
  ;;         forgiving :)
  ;; Note: pg will probably change HN tomorrow and break this whole fucking thing :/
  (let [tr (first ($ t "tr"))
        depth (-> tr
                  ($ "> td:eq(0) > img")
                  (.attr "width")
                  Long/valueOf)
        header ($ tr "> td:eq(2) > div:eq(0) > span")
        user   (-> header
                   ($ "> a:eq(0)")
                   .text)
        age    nil ;FIXME
        id     (-> header
                   ($ "> a")
                   second
                   (.attr "href")
                   (.substring (count "item?id=")))
        main ($1 tr "> td:eq(2) > span")
        paras (filter #(not= "reply" %) (all-text main))]
    {:depth depth
     :user  user
     :id    id
     :comment (vec paras)}))

(defn comments
  "Given an item id, return the comments as nested maps."
  [id]
  (let [trs (-> id
                comment-page-url
                URL.
                (Jsoup/parse 5000)
                .body
                ;; There are four "top-level" parts (tr's): toolbar, spacer,
                ;; comments-section, footer. Extract them and just keep the
                ;; third.
                ($ "> center > table > tbody > tr")
                (.eq 2)
                ;; The tr of interest further contains two tables: the
                ;; new-comment-form, and the comments.  We just want the latter.
                ($ "> td > table")
                (.eq 1)
                ;; Each row of this table contains a single comment, wrapped in
                ;; yet-another table, which itself has only one row.
                ($ "> tbody > tr > td > table > tr"))]
    (->> trs
         (map parse-comment trs))
    comments))


