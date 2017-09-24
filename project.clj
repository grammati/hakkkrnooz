(defproject hakkkrnooz "0.1.0-SNAPSHOT"
  :description "An alternative user-interface for Hacker News."

  :min-lein-version "2.0.0"
  :uberjar-name "hakkkrnooz-standalone.jar"
  :prep-tasks [["cljsbuild" "once" "min"] "compile"]

  :source-paths ["src/clj"]

  :plugins [[lein-cljsbuild "1.1.4"]]
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [org.jsoup/jsoup "1.10.2"]

                 [compojure "1.6.0"]
                 [hiccup "1.0.5"]

                 [cheshire "5.7.1"]

                 [prone "1.1.4"]
                 [clj-http "3.6.0"]
                 [ring/ring-core "1.6.1"]
                 [ring/ring-jetty-adapter "1.6.1"]
                 [ring "1.6.1"]
                 [org.clojure/core.async "0.3.443"]]

  :profiles
  {:cljs     {:source-paths ["src/cljs"]
              :dependencies [[org.clojure/clojurescript "1.9.562"]
                             [reagent "0.6.0"]
                             [re-frame "0.9.3"]
                             [reanimated "0.5.1"]
                             [cljs-ajax "0.6.0"]]}
   :cljs-dev {:plugins      [[lein-figwheel "0.5.10"]
                             [lein-less "1.7.5"]]
              :dependencies [[com.cemerick/piggieback "0.2.1"]
                             [figwheel-sidecar "0.5.10"]
                             [binaryage/devtools "0.9.4"]
                             [re-frisk "0.4.5"]]
              :less {:source-paths ["resources/less"]
                     :target-path "resources/public/css"}
              }
   :dev      [:cljs
              :cljs-dev
              {:source-paths ["dev"]
               :jvm-opts     ["-DCACHE_TTL=3000"]}]}

  :figwheel {:css-dirs     ["resources/public/css"]
             :ring-handler hakkkrnooz.web/handler}

  :repl-options {:init-ns dev
                 :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "hakkkrnooz.ui.core/mount-root"}
     :compiler     {:main                 hakkkrnooz.ui.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}
                    }}

    {:id           "min"
     :source-paths ["src/cljs"]
     :jar          true
     :compiler     {:main            hakkkrnooz.ui.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}]}

  :main hakkkrnooz.main)
