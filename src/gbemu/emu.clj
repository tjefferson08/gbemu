(ns gbemu.emu
  (:require [gbemu.cartridge :as cart]
            [gbemu.cpu :as cpu]))

;; Components
;; - Cartridge
;; - CPU
;; - Address Bus
;; - PPU
;; - Timer
(defn run
  ([ctx]
   (println (str "Tick " (:ticks (:emu ctx))))
  ;; TODO initialize graphics
  ;; Initialize true-type-fonts (TTF)
  ;; initialize CPU

  ;; rework to be clojureish
   (if (and (< (:ticks (:emu ctx)) 3) (:running (:emu ctx)))
     (if (:paused (:emu ctx))
       (or (delay 10) (recur ctx))
       (let [step-result (cpu/step ctx)]
         (if step-result
           (recur (update-in step-result [:emu :ticks] inc))
           -3)))
     0)))

(defn emu-cycles [cpu-cycles])
  ;; TODO

(defn delay [ms])
  ;; TODO sleep ms
