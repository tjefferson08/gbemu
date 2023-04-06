(ns gbemu.emu
  (:require [gbemu.cartridge :as cart]
            [gbemu.cpu.core :as cpu]))

;; Components
;; - Cartridge
;; - CPU
;; - Address Bus
;; - PPU
;; - Timer
(defn run
  ([ctx]
   ;; (println (str "Tick " (:ticks (:emu ctx))))
  ;; TODO initialize graphics
  ;; Initialize true-type-fonts (TTF)
  ;; initialize CPU

  ;; rework to be clojureish
   (if (and (< (:ticks (:emu ctx)) 10) (:running (:emu ctx)))
     (if (:paused (:emu ctx))
       (or (delay 10) (recur ctx))
       (let [;;_ (println (:cpu ctx))
             step-result (cpu/step ctx)
             _ (println (:cpu step-result) (:emu step-result))]
         (if (:halted (:cpu step-result))
           step-result
           (recur (update-in step-result [:emu :ticks] inc)))))
     0)))

(defn emu-cycles [cpu-cycles])
  ;; TODO

(defn delay [ms])
  ;; TODO sleep ms
