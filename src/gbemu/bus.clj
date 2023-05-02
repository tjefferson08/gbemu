(ns gbemu.bus
  (:require [gbemu.cartridge :as cart]
            [gbemu.ram :as ram]
            [gbemu.cpu.registers :as r]
            [gbemu.bytes :as b]
            [gbemu.io :as io]
            [gbemu.log :as log]
            [gbemu.clock :as clock]))

(defn- read* [ctx address]
  (b/to-unsigned
    (unchecked-byte
      (cond
        ;; 0x0000 - 0x3FFF : ROM Bank 0)
        ;; 0x4000 - 0x7FFF : ROM Bank 1 - Switchable
        (> 0x8000 address) (cart/read ctx address)

        ;; 0x9800 - 0x9BFF : BG Map 1
        ;; 0x9C00 - 0x9FFF : BG Map 2
        (> 0xA000 address) (or (log/stderr "Not implemented: BG Map") 0)

        ;; 0xA000 - 0xBFFF : Cartridge RAM
        (> 0xC000 address) (cart/read ctx address)

        ;; 0xC000 - 0xCFFF : RAM Bank 0
        ;; 0xD000 - 0xDFFF : RAM Bank 1-7 - switchable - Color only
        (> 0xE000 address) (ram/w-read ctx address)

        ;; 0xE000 - 0xFDFF : Reserved - Echo RAM
        (> 0xFE00 address) (or (log/stderr "Echo RAM: Not implemented TODO") 0)

        ;; 0xFE00 - 0xFE9F : Object Attribute Memory
        (> 0xFEA0 address) (or (log/stderr "OAM: Not implemented TODO") 0)

        ;; 0xFEA0 - 0xFEFF : Reserved - Unusable
        (> 0xFF00 address) (throw (Exception. "Reserved - Unusable"))

        ;; 0xFF00 - 0xFF7F : I/O Registers
        (> 0xFF80 address) (io/read ctx address)

        ;; 0xFF80 - 0xFFFE : high RAM
        (> 0xFFFF address) (ram/h-read ctx address)

        (= 0xFFFF address) (r/read-ie-reg ctx)

        :else (throw (Exception. (format "Unmapped bus address: %04X" address)))))))

(defn- write [ctx address value]
  (let [value (b/to-unsigned (unchecked-byte value))]
    (cond
      ;; 0x0000 - 0x3FFF : ROM Bank 0)
      ;; 0x4000 - 0x7FFF : ROM Bank 1 - Switchable
      (> 0x8000 address) (cart/write ctx address value)

      ;; 0x9800 - 0x9BFF : BG Map 1
      ;; 0x9C00 - 0x9FFF : BG Map 2
      (> 0xA000 address) (or (log/stderr "Not implemented: BG Map") ctx)

      ;; 0xA000 - 0xBFFF : Cartridge RAM
      (> 0xC000 address) (cart/write ctx address value)

      ;; 0xC000 - 0xCFFF : RAM Bank 0
      ;; 0xD000 - 0xDFFF : RAM Bank 1-7 - switchable - Color only
      (> 0xE000 address) (ram/w-write ctx address value)

      ;; 0xE000 - 0xFDFF : Reserved - Echo RAM
      (> 0xFE00 address) (or (log/stderr "Echo RAM: Not implemented TODO") ctx)

      ;; 0xFE00 - 0xFE9F : Object Attribute Memory
      (> 0xFEA0 address) (or (log/stderr "OAM: Not implemented TODO") ctx)

      ;; 0xFEA0 - 0xFEFF : Reserved - Unusable
      (> 0xFF00 address) (throw (Exception. "Reserved - Unusable"))

      ;; 0xFF00 - 0xFF7F : I/O Registers
      (> 0xFF80 address) (io/write ctx address value)

      ;; 0xFF80 - 0xFFFE : high RAM
      (> 0xFFFF address) (ram/h-write ctx address value)

      (= 0xFFFF address) (r/write-ie-reg ctx value)

      :else (throw (Exception. (format "Unmapped bus address: %04X" address))))))

(defn read
  "Tick `cycles` while reading bus, simulating real hardward timing & interrupts"
  ([ctx address]
   (read ctx address 0))

  ([ctx address cycles]
   (let [value (read* ctx address)]
     [value (clock/tick ctx cycles)])))

;; TODO remove after migrating to `read`
(defn read-bus
  "Tick `cycles` while reading bus, simulating real hardward timing & interrupts"
  [ctx address]
  (read* ctx address))

(defn write-bus
 ([ctx address value]
  (write-bus ctx address value 0))

 ([ctx address value cycles]
  (clock/tick (write ctx address value) cycles)))
  

(defn write-bus-16 [ctx address value]
  (-> ctx
     (write-bus (inc address) (bit-shift-right value 8) 4)
     (write-bus address (bit-and 0x00FF value) 4)))

(comment

 nil)
