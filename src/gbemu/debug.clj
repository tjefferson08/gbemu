(ns gbemu.debug
  (:require [gbemu.bus :as bus]
            [gbemu.log :as log]))

(defn init [] {:msg []})

(defn update [ctx]
  (if (= 0x81 (bus/read-bus ctx 0xFF02))
    (let [c (bus/read-bus ctx 0xFF01)]
      (-> ctx (update-in [:debug :msg] conj c)
              (bus/write-bus 0xFF02 0)))
    ctx))

(defn print [ctx]
  (let [m (get-in ctx [:debug :msg])]
    (or (empty? m) (log/stderr (apply str "SERIAL: " (map char m))))))

(comment
 (apply str (map char [48 49 45 115 112 101 99 105 97 108 10 10]))

 (.println *err* (apply str "SERIAL: " (map char [48 49])))

 ,)
