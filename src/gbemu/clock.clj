(ns gbemu.clock
  (:require [gbemu.timer :as timer]))

(defn tick [ctx cycles]
  (if (zero? cycles)
    ctx
    (let [ctx' (-> ctx (timer/tick))]
      (recur ctx' (dec cycles)))))
