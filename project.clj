(defproject hakkkrnooz "0.1.0-SNAPSHOT"
  :description "An alternative user-interface for Hacker News."
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.jsoup/jsoup "1.6.1"]

                 [compojure "1.0.1"]
                 [hiccup "0.3.8"]

                 [cheshire "2.2.0"]

                 [ring/ring-jetty-adapter "1.1.0-RC1"]
                 ]
  :ring {:handler hakkkrnooz.web/app-routes}
  :main hakkkrnooz.web
  )
