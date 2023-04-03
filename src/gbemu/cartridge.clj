(ns gbemu.cartridge
  (:require [clojure.java.io :as io]
            [gbemu.bytes :as bytes]))

(def rom-types
 {0x00 "ROM ONLY",
  0x01 "MBC1",
  0x02 "MBC1+RAM",
  0x03 "MBC1+RAM+BATTERY",
  0x05 "MBC2",
  0x06 "MBC2+BATTERY",
  0x08 "ROM+RAM 1",
  0x09 "ROM+RAM+BATTERY 1",
  0x0B "MMM01",
  0x0C "MMM01+RAM",
  0x0D "MMM01+RAM+BATTERY",
  0x0F "MBC3+TIMER+BATTERY",
  0x10 "MBC3+TIMER+RAM+BATTERY",
  0x11 "MBC3",
  0x12 "MBC3+RAM 2",
  0x13 "MBC3+RAM+BATTERY 2",
  0x19 "MBC5",
  0x1A "MBC5+RAM",
  0x1B "MBC5+RAM+BATTERY",
  0x1C "MBC5+RUMBLE",
  0x1D "MBC5+RUMBLE+RAM",
  0x1E "MBC5+RUMBLE+RAM+BATTERY",
  0x20 "MBC6",
  0x22 "MBC7+SENSOR+RUMBLE+RAM+BATTERY"
  0xFC "POCKET CAMERA"
  0xFD "BANDAI TAMA5"
  0xFE "HuC3"
  0xFF "HuC1+RAM+BATTERY"})

(defn exp [x n]
  (reduce * (repeat n x)))

(defn cartridge->header [c-bytes]
  (let [barr c-bytes]
    {:entry-point (bytes/slice barr 0x100 0x103)
     :nintendo-logo (bytes/slice barr 0x104 0x133)
     :title (bytes/slice barr 0x0134 0x0143)
     :manufacturer-code (bytes/slice barr 0x13F 0x142)
     :cgb-flag (aget barr 0x143)
     :new-licensee-code (bytes/slice barr 0x144 0x145)
     :sgb-flag (aget barr 0x146)
     :cartridge-type (aget barr 0x147)
     :rom-size (aget barr 0x148)
     :ram-size (aget barr 0x149)
     :destination-code (aget barr 0x14A)
     :old-licensee-code (aget barr 0x14B)
     :mask-rom-version-number (aget barr 0x14C)
     :header-checksum (aget barr 0x14D)
     :global-checksum (bytes/slice barr 0x14E 0x14F)}))

(defn header-checksum [cartridge-bytes]
  (let [bvec (vec (bytes/slice cartridge-bytes 0x134 0x14D))]
    (reduce (fn [acc val] (unchecked-byte (- acc val 1))) 0 bvec)))

(defn load-cartridge [f]
  (let [cartridge-bytes (bytes/slurp-bytes f)
        header (cartridge->header cartridge-bytes)]
    {:filename f
     :rom-bytes cartridge-bytes
     :title (apply str (map char (filter #(not= 0x0 %) (:title header))))
     :rom-type (get rom-types (:cartridge-type header) "???")
     :rom-size (str (* 32 (exp 2 (:rom-size header))) "KB")
     :header-checksum-provided (bytes/hexify [(:header-checksum header)])
     :header-checksum-computed (bytes/hexify [(header-checksum cartridge-bytes)])
     :header header}))

;; Rather than use def + atom, maybe we can orchestrate component initialization with integrant?
;; bus depends on cart (and more)
;; cart depends on ROM
(defn read [ctx address]
   (aget (get-in ctx [:cartridge :rom-bytes]) address))

(defn write [ctx address value])

(comment

  (vec (.getBytes "hello"))
  (class (.getBytes "hello"))

  (bytes/hexify (.getBytes "DMG-ACID2"))

  (let [barr (.getBytes "hello-world")
        acid-rom-barr (bytes/slurp-bytes (io/resource "roms/dmg-acid2.gb"))
        acid-title (java.util.Arrays/copyOfRange acid-rom-barr 0x134 0x137)
        acid-byte (aget acid-rom-barr 0x101)]
    (hexify acid-title))

  (load-cartridge (io/resource "roms/dmg-acid2.gb"))

  0x100
  0x10f
  0x10F

  nil)
