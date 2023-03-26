(ns gbemu.cartridge
  (:require [clojure.java.io :as io]))

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

(defn file->bytes [f]
  (.getBytes (slurp f)))

(defn- byte-slice [barr from to]
  (java.util.Arrays/copyOfRange barr from to))

(defn cartridge->header [f]
  (let [barr (slurp-bytes f)]
    {:entry-point (byte-slice barr 0x100 0x103)
     :nintendo-logo (byte-slice barr 0x104 0x133)
     :title (byte-slice barr 0x0134 0x0143)
     :manufacturer-code (byte-slice barr 0x13F 0x142)
     :cgb-flag (aget barr 0x143)
     :new-licensee-code (byte-slice barr 0x144 0x145)
     :sgb-flag (aget barr 0x146)
     :cartridge-type (aget barr 0x147)
     :rom-size (aget barr 0x148)
     :ram-size (aget barr 0x149)
     :destination-code (aget barr 0x14A)
     :old-licensee-code (aget barr 0x14B)
     :mask-rom-version-number (aget barr 0x14C)
     :header-checksum (aget barr 0x14D)
     :global-checksum (byte-slice barr 0x14E 0x14F)}))

(defn load [f]
  (let [header (cartridge->header f)]
    {:filename f
     :title (apply str (map char (filter #(not= 0x0 %) (:title header))))
     :rom-type (get rom-types (:cartridge-type header) "???")
     :rom-size (:rom-size header)
     :header header}))

(defn hexify "Convert byte sequence to hex string" [coll]
  (let [hex [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9 \A \B \C \D \E \F]]
      (letfn [(hexify-byte [b]
               (let [v (bit-and b 0xFF)]
                 [(hex (bit-shift-right v 4)) (hex (bit-and v 0x0F))]))]
        (apply str (mapcat hexify-byte coll)))))

(defn hexify-str [s]
  (hexify (.getBytes s)))

(defn slurp-bytes
  "Slurp the bytes from a slurpable thing"
  [x]
  (with-open [in (clojure.java.io/input-stream x)
              out (java.io.ByteArrayOutputStream.)]
    (clojure.java.io/copy in out)
    (.toByteArray out)))

(comment
  (vec (.getBytes "hello"))
  (class (.getBytes "hello"))

  (hexify (.getBytes "DMG-ACID2"))

  (let [barr (.getBytes "hello-world")
        acid-rom-barr (slurp-bytes (io/resource "roms/dmg-acid2.gb"))
        acid-title (java.util.Arrays/copyOfRange acid-rom-barr 0x134 0x137)
        acid-byte (aget acid-rom-barr 0x101)]
    (hexify acid-title))

  (= (.getBytes (slurp (io/resource "roms/dmg-acid2.gb")))
     (slurp-bytes (io/resource "roms/dmg-acid2.gb")))

  (map hexify (partition 64 (.getBytes (slurp (io/resource "roms/01-special.gb")))))

  (load (io/resource "roms/dmg-acid2.gb"))


  0x100
  0x10f
  0x10F

  (Integer/toString -65 16)

  nil)
