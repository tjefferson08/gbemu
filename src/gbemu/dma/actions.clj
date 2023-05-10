(ns gbemu.dma.actions
  (:require [gbemu.ppu :as ppu]
            [gbemu.bus.logical :as bus]))

(defn tick [{dma :dma :as ctx}]
 (cond
   (not (:active dma)) ctx
   (pos? (:start-delay dma)) (update-in ctx [:dma :start-delay] dec)
   :else (let [offset     (:byte dma)
               read-addr  (* 0x100 (:value dma))
               value      (bus/read! ctx (+ offset read-addr))
               offset'    (inc offset)
               active'    (<= offset' 0x9F)]
            (println "writing dma!")
            (-> ctx (ppu/write-oam offset value)
                    (update :dma assoc :byte offset', :active active')))))
