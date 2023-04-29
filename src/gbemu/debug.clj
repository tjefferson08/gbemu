(ns gbemu.debug
  (:require [gbemu.bus :as bus]))

(defn init [] {:msg []})

(defn update [ctx]
  (if (= 0x81 (bus/read-bus ctx 0xFF02))
    (let [c (bus/read-bus ctx 0xFF01)]
      (-> ctx (update-in [:debug :msg] conj c)
              (bus/write-bus 0xFF02 0)))
    ctx))

(defn print [ctx]
  (let [m (get-in ctx [:debug :msg])]
    (or (empty? m) (println "SERIAL:" m))))
