(ns gbemu.clock)

(defn tick [ctx cycles]
  (if (zero? cycles)
    ctx
    (recur ctx (dec cycles))))
