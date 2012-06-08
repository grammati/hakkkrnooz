(defproject hakkkrnooz "0.1.0-SNAPSHOT"
  :description "An alternative user-interface for Hacker News."
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.jsoup/jsoup "1.6.1"]

                 [compojure "1.1.0"]
                 [hiccup "1.0.0"]

                 [cheshire "4.0.0"]

                 [ring/ring-jetty-adapter "1.1.0"]
                 ]
  :ring {:handler hakkkrnooz.web/app-routes}
  )
