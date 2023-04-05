(ns gbemu.system-test
  (:require [gbemu.system :as sut]
            [clojure.test :refer :all]
            [gbemu.bytes :as bytes]
            [gbemu.cpu.registers :as r]))

(defn ctx-with [{:keys [instructions]}]
  (let [header-bytes (bytes/slurp-bytes "resources/roms/header-only.gb")
        rom-file     (clojure.java.io/file "/tmp/tempfile-rom.gb")
        new-rom-bytes (byte-array (concat header-bytes instructions))
        _            (bytes/spit-bytes rom-file new-rom-bytes)]
     (sut/boot rom-file)))



(deftest ^:integration stack-operations
  (let [ctx (ctx-with {:instructions [0x76 0xC5 0xD5 0xE5 0xF5 0x76]})]
    (is (= 0x99 (r/read-reg ctx :pc)))))
