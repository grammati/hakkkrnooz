(defproject hakkkrnooz "0.1.0-SNAPSHOT"
  :description "An alternative user-interface for Hacker News."
  :dependencies [[org.clojure/clojure "1.9.0-alpha15"]
                 [org.jsoup/jsoup "1.10.2"]

                 [compojure "1.6.0"]
                 [hiccup "1.0.5"]

                 [cheshire "5.7.1"]

                 [org.immutant/web "2.1.6"]

                 [prone "1.1.4"]
                 [clj-http "3.5.0"]
                 [ring/ring-core "1.6.1"]
                 [ring/ring-jetty-adapter "1.6.1"]
                 [ring "1.6.1"]]
  :profiles {:dev {:jvm-opts ["-DCACHE_TTL=3000"]}
             :source-paths ["dev"]}
  :main hakkkrnooz.main)
