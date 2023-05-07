(ns gbemu.emu
  (:require [gbemu.cartridge :as cart]
            [gbemu.cpu.core :as cpu]
            [gbemu.state :refer [*ctx]]))

(def TICK_LIMIT 100100)

(defn init []
  {:paused false, :running true, :ticks 0, :headless false})

;; Components
;; - Cartridge
;; - CPU
;; - Address Bus
;; - PPU
;; - Timer
(defn run
  ([{{:keys [tick-limit headless ticks]} :emu :as ctx}]
   ;; (println (str "Tick " (:ticks (:emu ctx))))
  ;; TODO initialize graphics
  ;; Initialize true-type-fonts (TTF)
  ;; initialize CPU
   (if (and tick-limit (< tick-limit ticks))
     (throw (Exception. "TICK LIMIT EXCEEDED")))

  ;; rework to be clojureish
   (if (:running (:emu ctx))
     (if (:paused (:emu ctx))
       (or (delay 10) (recur ctx))
       (let [
             ;; _ (println "before step" (:cpu ctx))
             step-result (cpu/step ctx)]
             ;; step-result (if (zero? (mod ticks 100))
             ;;               (persistent! (transient step-result))
             ;;               step-result)]
             ;; _ (println "after step" (:cpu step-result))]
          (swap! *ctx step-result)
          (recur (update-in step-result [:emu :ticks] inc))))
     ctx)))

(defn delay [ms])
  ;; TODO sleep ms
