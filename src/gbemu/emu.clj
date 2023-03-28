(ns gbemu.emu)

(def ctx (atom {:paused false, :running false, :ticks 0}))

;; Components
;; - Cartridge
;; - CPU
;; - Address Bus
;; - PPU
;; - Timer
(defn run [rom-file]
  ;; check for ROM file
  ;; attempt cartrige load
  ;; TODO initialize graphics
  ;; Initialize true-type-fonts (TTF)
  ;; initialize CPU

  (reset! ctx {:running true, :paused false, :ticks 0})

  ;; rework to be clojureish
  (while (:running @ctx)
    (if (:paused @ctx)
      (delay 10)
      (if (not (cpu-step)) -3)
      (swap! ctx update-in :ticks inc))))

(defn emu-cycles [cpu-cycles])
  ;; TODO

(defn delay [ms])
  ;; TODO sleep ms
