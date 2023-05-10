;; TODO rename to bus.simulated or similar
(ns gbemu.bus
  (:require [gbemu.clock :as clock]
            [gbemu.bus.logical :as b]))

(defn read
  "Tick `cycles` while reading bus, simulating real hardward timing & interrupts.
   Returns tuple of read-result and new, potentially changed context"
  ([ctx address]
   (read ctx address 4))

  ([ctx address cycles]
   (let [value (b/read! ctx address)]
     [value (clock/tick ctx cycles)])))

(defn write
 ([ctx address value]
  (write ctx address value 4))

 ([ctx address value cycles]
  (clock/tick (b/write! ctx address value) cycles)))

(defn write-bus
 ([ctx address value]
  (write-bus ctx address value 4))

 ([ctx address value cycles]
  (clock/tick (write ctx address value) cycles)))
  
(defn write-bus-16 [ctx address value]
  (-> ctx
     (write-bus (inc address) (bit-shift-right value 8))
     (write-bus address (bit-and 0x00FF value))))

(defn mem-string [ctx addr]
  (format "0x%04X:%02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X"
    addr
    (b/read! ctx (+ 0x0 addr))
    (b/read! ctx (+ 0x1 addr))
    (b/read! ctx (+ 0x2 addr))
    (b/read! ctx (+ 0x3 addr))
    (b/read! ctx (+ 0x4 addr))
    (b/read! ctx (+ 0x5 addr))
    (b/read! ctx (+ 0x6 addr))
    (b/read! ctx (+ 0x7 addr))
    (b/read! ctx (+ 0x8 addr))
    (b/read! ctx (+ 0x9 addr))
    (b/read! ctx (+ 0xA addr))
    (b/read! ctx (+ 0xB addr))
    (b/read! ctx (+ 0xC addr))
    (b/read! ctx (+ 0xD addr))
    (b/read! ctx (+ 0xE addr))
    (b/read! ctx (+ 0xF addr))))

(comment

 nil)
