(ns hakkkrnooz.scrape
  (:import (org.jsoup Jsoup)
           (org.jsoup.nodes Node Document Element TextNode)
           (org.jsoup.select Selector Elements)))


(set! *warn-on-reflection* true)

(defprotocol Searchable
  ($ ^Elements [e q]))

(extend-protocol Searchable
  Element
  ($ ^Elements [e ^String q] (Selector/select q e))
  Iterable
  ($ ^Elements [e ^String q] (Selector/select q e)))

(defn $1 ^Element [elt selector]
  (.first ($ elt selector)))

(defn block? [n]
  (and (instance? Element n)
       (.isBlock ^Element n)))

(defn inline? [n]
  (not (block? n)))

(defn comment-text [^Element span]
  ;; see the explanation of parse-comment, below. Briefly, span can be:
  ;; 1) -span
  ;;      -font
  ;;        - single text node  (note: reply-link is not in here)
  ;; or:
  ;; 2) -span
  ;;      -font
  ;;        -text node (usually?)
  ;;        -p or pre or text or ...? (0..n)
  ;;      - second-to-last p
  ;;      - reply-link
  (let [[font extra-p] (seq (.childNodes span))
        items (if (instance? Element font)
                (concat (.childNodes ^Element font) (if extra-p (list extra-p)))
                (list font)             ;"deleted" comment
                )]
    (loop [[item & items :as all] items
           blocks []]
      (if-not item
        blocks
        (if (block? item)
          (recur items (conj blocks (str item)))
          (let [[inl more] (split-with inline? all)]
            (recur more (conj blocks (apply str inl)))))))))

(defn parse-comment
  "Parse the data out of the markup for a single comment on HN."
  [^Element tr]
  ;; The markup on the HN comments page is rather bizarre, but easy
  ;; enough to extract the data from.
  ;; tr is a row with 3 cells
  ;;  - the first is a spacer, where the width of an image determines the nesting
  ;;  - the second contains the upvote button
  ;;  - the third contains everything else - specifically, it contains:
  ;;    - first, a div containing a span containing either:
  ;;      - all three of:
  ;;        - the author's handle, and
  ;;        - the text "<N> hours ago |", and
  ;;        - a link (which gives us the comment-id)
  ;;      - or - nothing, if the comment has been deleted
  ;;    - then there is a br (which seems to compensate for the -10px
  ;;      bottom-margin on the div above :/)
  ;;    - then, there are two possibilities:
  ;;      1) For a single-paragraph comment, there is a span containing a font
  ;;         tag (really) that contains the text of the comment, and then there
  ;;         is a p tag containing the reply link.
  ;;      2) For a multi-paragraph comment, the last paragraph seems to get spit
  ;;         out of the end of the font tag to become a child of the span, and
  ;;         the reply-link gets sucked into the span. Someone seems to have
  ;;         worked around the first of these oddities by wrapping another font
  ;;         tag around the text of the spat-out paragraph to get the color
  ;;         right. Furthermore, the first paragraph under the font tag is not
  ;;         wrapped in a p tag, but all the rest are. And yes, in case you are
  ;;         wondering, the span that contains all these paragraphs is an inline
  ;;         element, and it's full of block elements. Good thing browsers are
  ;;         forgiving :)
  ;; Note: pg will probably change HN tomorrow and break this whole fucking thing :/
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
                     ($ "> a")
                     (.get 1)
                     (.attr "href")
                     (.substring (count "item?id="))))
        main   ($1 tr "> td:eq(2) > span")
        paras  (comment-text main)]
    {:depth   depth
     :user    user
     :id      id
     :comment paras}))

(defn nest-comments [cs]
  (loop [[c & cs] (seq cs)
         tlc []]                        ;top-level-comments
    (if (nil? c)
      tlc
      (let [[desc others] (split-with #(> (:depth %) (:depth c)) cs)
            c (assoc c :replies (nest-comments desc))]
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
                        (:user c) " -> " (left (first (:comment c)) n) "...")
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


(defn parse-comments
  "Extract the comments from the HTML, as a Jsoup Document. "
  ([^Document doc]
     (let [trs (-> doc                   
                   .body
                   ;; There are four "top-level" parts (tr's): toolbar, spacer,
                   ;; comments-section, footer. Extract them and just keep the
                   ;; third.
                   ($ "> center > table > tbody > tr")
                   (.get 2)
                   ;; The tr of interest further contains two tables: the
                   ;; new-comment-form, and the comments.  We just want the latter.
                   ($ "> td > table")
                   (.get 1)
                   ;; Each row of this table contains a single comment, wrapped in
                   ;; yet-another table, which itself has only one row.
                   ($ "> tbody > tr > td > table > tbody > tr"))]
       (->> trs
            (map parse-comment)
            nest-comments
            ))))