(ns gbemu.cpu.interrupt
  (:require [gbemu.bytes :as bytes]
            [gbemu.cpu.registers :as r]))

(def INTERRUPT_BITS {:vblank 0, :lcd-stat 1, :timer 2, :serial 3, :joypad 4})
(def INTERRUPT_ROUTINES {:vblank 0x40, :lcd-stat 0x48, :timer 0x50, :serial 0x58, :joypad 0x60})

(defn routine-for [type]
  (INTERRUPT_ROUTINES type))

(defn request [ctx type]
  (let [bit (INTERRUPT_BITS type)]
    (update-in ctx [:cpu :int-flags] bit-set bit)))

(defn clear [ctx type]
  (let [bit (INTERRUPT_BITS type)]
    (update-in ctx [:cpu :int-flags] bit-clear bit)))

(defn ready? [ctx type]
  (let [bit (INTERRUPT_BITS type)
        rdy? (bit-test (get-in ctx [:cpu :int-flags]) bit)
        enabled? (bit-test (get-in ctx [:cpu :ie-register]) bit)]
     (and rdy? enabled?)))
