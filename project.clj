(defproject hakkkrnooz "0.1.0-SNAPSHOT"
  :description "An alternative user-interface for Hacker News."

  :min-lein-version "2.0.0"
  :uberjar-name "hakkkrnooz-standalone.jar"

  :dependencies [[org.clojure/clojure "1.9.0-beta1"]
                 [org.jsoup/jsoup "1.10.3"]

                 [compojure "1.6.0"]
                 [hiccup "1.0.5"]

                 [cheshire "5.8.0"]

                 [prone "1.1.4"]
                 [clj-http "3.7.0"]
                 [ring/ring-core "1.6.2"]
                 [ring/ring-jetty-adapter "1.6.2"]
                 [ring "1.6.2"]]

  :profiles {:dev {:jvm-opts ["-DCACHE_TTL=3000"]}
             :source-paths ["dev"]}

  :main hakkkrnooz.main)
