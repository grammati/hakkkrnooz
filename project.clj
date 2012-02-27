(defproject hakkkrnooz "0.1.0-SNAPSHOT"
  :description "An alternative user-interface for Hacker News."
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.jsoup/jsoup "1.6.1"]

                 [compojure "1.0.1"]
                 [hiccup "0.3.8"]

                 [cheshire "2.2.0"]

                 ]
  :ring {:handler hakkkrnooz.web/app-routes}
  )
