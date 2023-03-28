(ns gbemu.emu
  (:require [gbemu.cpu :as cpu]))

(defn init [] {:paused false, :running false, :ticks 0})

;; Components
;; - Cartridge
;; - CPU
;; - Address Bus
;; - PPU
;; - Timer
(defn run [ctx rom-file]
  ;; check for ROM file
  ;; attempt cartrige load
  ;; TODO initialize graphics
  ;; Initialize true-type-fonts (TTF)
  ;; initialize CPU

  ;; (reset! ctx {:running true, :paused false, :ticks 0})

  ;; rework to be clojureish
  (while (:running ctx)
    (if (:paused ctx)
      (delay 10)

      ;; this is the wrong ctx, need CPU ctx
      (if (not (cpu/step ctx)) -3))))
      ;; (swap! ctx update-in :ticks inc))))

(defn emu-cycles [cpu-cycles])
  ;; TODO

(defn delay [ms])
  ;; TODO sleep ms
