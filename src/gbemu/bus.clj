(ns gbemu.bus
  (:require [gbemu.cartridge :as cart]))

(defn read-bus [ctx address]
  ;; (println (str "read bus" ctx address))
  (cond (> 0x8000 address) (cart/read-rom ctx address)
    :else (throw (Exception. "Not implemented"))))

(defn write-bus [ctx address value]
  (cond (> 0x8000 address) (cart/write-rom address value)
    :else (throw (Exception. "Not implemented"))))

(defn read-bus-16 [ctx address]
  (let [lo (read-bus address)
        hi (read-bus (inc address))]
    (bit-or lo (bit-shift-left hi 8))))

(defn write-bus-16 [ctx address value]
  (-> ctx
     (write-bus (inc address) (bit-shift-right value 8))
     (write-bus address (bit-and 0x00FF value))))


; 0x0000 - 0x3FFF : ROM Bank 0)
; 0x4000 - 0x7FFF : ROM Bank 1 - Switchable
; 0x8000 - 0x97FF : CHR RAM
; 0x9800 - 0x9BFF : BG Map 1
; 0x9C00 - 0x9FFF : BG Map 2
; 0xA000 - 0xBFFF : Cartridge RAM
; 0xC000 - 0xCFFF : RAM Bank 0
; 0xD000 - 0xDFFF : RAM Bank 1-7 - switchable - Color only
; 0xE000 - 0xFDFF : Reserved - Echo RAM
; 0xFE00 - 0xFE9F : Object Attribute Memory
; 0xFEA0 - 0xFEFF : Reserved - Unusable
; 0xFF00 - 0xFF7F : I/O Registers
; 0xFF80 - 0xFFFE : Zero Page

(comment

 nil)
