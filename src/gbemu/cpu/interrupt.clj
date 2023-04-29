(ns gbemu.cpu.interrupt
  (:require [gbemu.bytes :as bytes]))

(def INTERRUPT_BITS {:vblank 0, :lcd-stat 1, :timer 2, :serial 3, :joypad 4})

(defn request-interrupt [ctx type]
  (let [bit (INTERRUPT_BITS type)]
    (update-in ctx [:cpu :int-flags] (fn [fl] (bit-set fl bit)))))

(defn read-flags [ctx]
  (get-in ctx [:cpu :int-flags]))

(defn write-flags [ctx value]
  (assoc-in ctx [:cpu :int-flags] (bytes/to-u8 value)))

(defn read-ie-reg [ctx]
  (get-in ctx [:cpu :ie-register]))

(defn write-ie-reg [ctx value]
  (assoc-in ctx [:cpu :ie-register] (bytes/to-u8 value)))
