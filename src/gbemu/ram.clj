(ns gbemu.ram)

(defn init []
  {:wram (apply vector-of :byte (repeat 0x2000 0))
   :hram (apply vector-of :byte (repeat 0x80 0))})

(defn w-read [ctx address]
  (let [addr (- address 0xC000)]
    (if (> addr 0x2000)
      (throw (Exception. (format "Invalid WRAM address %08X" address)))
      (get-in ctx [:ram :wram addr]))))

(defn w-write [ctx address value]
  (let [addr (- address 0xC000)]
    (if (> addr 0x2000)
      (throw (Exception. (format "Invalid WRAM address %08X" address)))
      (assoc-in ctx [:ram :wram addr] value))))

(defn h-read [ctx address]
  (let [addr (- address 0xFF80)]
    (if (> addr 0x80)
      (throw (Exception. (format "Invalid HRAM address %08X" address)))
      (get-in ctx [:ram :hram addr]))))

(defn h-write [ctx address value]
  (let [addr (- address 0xFF80)]
    (if (> addr 0x80)
      (throw (Exception. (format "Invalid HRAM address %08X" address)))
      (assoc-in ctx [:ram :hram addr] value))))

(comment
  (println (aset (byte-array 5) 2 0x01))
  (vector-of :byte 0x01 0xF)
  (assoc-in {:a {:b [1 2 3]}} [:a :b 2] 9)

 nil)
