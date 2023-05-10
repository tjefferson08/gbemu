(ns gbemu.dma.state)

(defn init []
  {:dma {:active false, :byte 0, :start-delay 0, :value 0}})

(defn transferring? [ctx]
  (get-in ctx [:dma :active]))

(defn start [ctx start-byte]
  (println "STARTING DMA")
  (update ctx :dma assoc :active true
                         :byte 0
                         :start-delay 8
                         :value start-byte))
