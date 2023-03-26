(ns gbemu.system
  (:require [integrant.core :as ig])
  (:gen-class))

(def config {})

(defn -main []
  (ig/init config))
