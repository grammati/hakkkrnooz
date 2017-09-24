(ns hakkkrnooz.ui.keys)

(defn show-children [elt]
  (println "show-children" (.-id elt)))

(defn handle-keydown [evt]
  ;; return if evt.ctrlKey or evt.altKey or evt.shiftKey
  (when-not (or (.-ctrlKey evt) (.-altKey evt) (.-shiftKey evt))
    (when-let [f (case (.-keyCode evt)
                   76 show-children
                   nil)]
      (f (.-target evt)))))
