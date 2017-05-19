(ns hakkkrnooz.main
  (:require [hakkkrnooz.web]))

(defn -main [& [port]]
  (hakkkrnooz.web/start port))
