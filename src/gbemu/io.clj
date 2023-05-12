(ns gbemu.io
  (:require [gbemu.timer :as timer]
            [gbemu.cpu.registers :as r]
            [gbemu.dma.state :as dma]
            [gbemu.lcd :as lcd]))

(defn init [] {:serial-data [0 0]})

(defn read [ctx address]
  (cond
    (= 0xFF01 address)         (get-in ctx [:io :serial-data 0])
    (= 0xFF02 address)         (get-in ctx [:io :serial-data 1])
    (= 0xFF03 address)         (timer/read ctx address)
    (= 0xFF04 address)         (timer/read ctx address)
    (= 0xFF05 address)         (timer/read ctx address)
    (= 0xFF06 address)         (timer/read ctx address)
    (= 0xFF07 address)         (timer/read ctx address)
    (= 0xFF0F address)         (r/read-interrupt-flags ctx)
    (<= 0xFF40 address 0xFF4B) (lcd/read ctx address)
    :else 0))
     ;; (do (println (format "Unimplemented IO register %08X" address))
     ;;     0)))
    ;; :else (throw (Exception. (format "Unimplemented IO register %08X" address)))))

(defn write [ctx address value]
  (cond
    (= 0xFF01 address)         (assoc-in ctx [:io :serial-data 0] value)
    (= 0xFF02 address)         (assoc-in ctx [:io :serial-data 1] value)
    (= 0xFF03 address)         (assoc-in ctx [:io :serial-data 1] value)
    (= 0xFF04 address)         (timer/write ctx address value)
    (= 0xFF05 address)         (timer/write ctx address value)
    (= 0xFF06 address)         (timer/write ctx address value)
    (= 0xFF07 address)         (timer/write ctx address value)
    (= 0xFF0F address)         (r/write-interrupt-flags ctx value)
    (<= 0xFF40 address 0xFF4B) (lcd/write ctx address value)
    :else ctx))
     ;; (do (println (format "Unimplemented IO register %08X" address))
     ;;     ctx)))
    ;; :else (throw (Exception. (format "Unimplemented IO register %08X" address)))))
