(ns gbemu.cartridge-test
  (:require [gbemu.cartridge :as sut]
            [clojure.test :refer :all]
            [gbemu.bytes :as b]
            [clojure.java.io :as io]))

(defn roms []
  (let [dmg-acid2-rom (io/resource "roms/dmg-acid2.gb")]
    {:dmg-acid2-rom dmg-acid2-rom
     :dmg-acid2-rom-bytes (b/slurp-bytes dmg-acid2-rom)}))

(deftest load-known-cartridge
  (let [ctx (sut/load-cartridge (:dmg-acid2-rom (roms)))]
    (is (= "9F" (:header-checksum-provided ctx)))
    (is (= "9F" (:header-checksum-computed ctx)))))
