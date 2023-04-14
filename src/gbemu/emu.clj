(ns gbemu.emu
  (:require [gbemu.cartridge :as cart]
            [gbemu.cpu.core :as cpu]))

(def TICK_LIMIT 200)

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
   (if (and TICK_LIMIT (< TICK_LIMIT (get-in ctx [:emu :ticks])))
     (throw (Exception. "TICK LIMIT EXCEEDED")))

  ;; rework to be clojureish
   (if (:running (:emu ctx))
     (if (:paused (:emu ctx))
       (or (delay 10) (recur ctx))
       (let [
             ;; _ (println "before step" (:cpu ctx))
             step-result (cpu/step ctx)]
             ;; _ (println "after step" (:cpu step-result))]
         (if (:halted (:cpu step-result))
           step-result
           (recur (update-in step-result [:emu :ticks] inc)))))
     0)))

(defn emu-cycles [cpu-cycles])
  ;; TODO

(defn delay [ms])
  ;; TODO sleep ms
