(ns gbemu.io
  (:require [gbemu.timer :as timer]
            [gbemu.cpu.interrupt :as interrupt]))

(defn init [] {:serial-data [0 0]})

(defn read [ctx address]
  (case address
    0xFF01 (get-in ctx [:io :serial-data 0])
    0xFF02 (get-in ctx [:io :serial-data 1])
    0xFF03 (timer/read ctx address)
    0xFF04 (timer/read ctx address)
    0xFF05 (timer/read ctx address)
    0xFF06 (timer/read ctx address)
    0xFF07 (timer/read ctx address)
    0xFF0F (interrupt/read-flags ctx)
     (do (println (format "Unimplemented IO register %08X" address))
         0)))
    ;; :else (throw (Exception. (format "Unimplemented IO register %08X" address)))))

(defn write [ctx address value]
  (println "***WRITINGIO****" address value)
  (case address
    0xFF01 (assoc-in ctx [:io :serial-data 0] value)
    0xFF02 (assoc-in ctx [:io :serial-data 1] value)
    0xFF03 (assoc-in ctx [:io :serial-data 1] value)
    0xFF04 (timer/write ctx address value)
    0xFF05 (timer/write ctx address value)
    0xFF06 (timer/write ctx address value)
    0xFF07 (timer/write ctx address value)
    0xFF0F (interrupt/write-flags ctx value)
     (do (println (format "Unimplemented IO register %08X" address))
         ctx)))
    ;; :else (throw (Exception. (format "Unimplemented IO register %08X" address)))))
