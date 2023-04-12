(ns gbemu.execution.xor
  (:require [gbemu.execution.flags :as flags]))

(defn xor [ctx]
  (let [a    (get-in ctx [:cpu :registers :a])
        data (get-in ctx [:cpu :fetched-data])
        res  (bit-xor a (bit-and data 0xFF))]
    (-> ctx
        (assoc-in [:cpu :registers :a] res)
        (flags/set-flags {:z (zero? res)}))))
