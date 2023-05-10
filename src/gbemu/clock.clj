(ns gbemu.clock
  (:require [gbemu.timer :as timer]
            [gbemu.dma.actions :as dma]))

(defn tick [ctx cycles]
  (if (zero? cycles)
    ctx
    (let [dma-tick (if (zero? (mod cycles 4)) dma/tick identity)
          ctx' (-> ctx timer/tick dma-tick)]
      (recur ctx' (dec cycles)))))


(comment
  (mod 12 4)

 ,,,)
