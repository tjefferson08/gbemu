(ns gbemu.system-test
  (:require [gbemu.system :as sut]
            [clojure.test :refer :all]
            [gbemu.bytes :as bytes]
            [gbemu.cpu.registers :as r]))

(defn ctx-with [{:keys [instructions]}]
  (let [header-bytes (bytes/slurp-bytes "resources/roms/header-only.gb")
        rom-file     (clojure.java.io/file "/tmp/tempfile-rom.gb")
        new-rom-bytes (byte-array (take 0x8000 (concat header-bytes instructions (repeat 0x00))))
        _            (bytes/spit-bytes rom-file new-rom-bytes)]
     (sut/boot rom-file)))



(deftest ^:integration stack-operations
  (let [ctx (ctx-with {:instructions [
                                      0x01 0xCC 0xBB ;; LD BC, 0xBBCC
                                      0xC5           ;; PUSH BC
                                      0x11 0xEE 0xDD ;; LD DE, 0xDDEE
                                      0xD5           ;; PUSH DE
                                      0x21 0x34 0x12 ;; LD HL, 0x1234
                                      0xE5           ;; PUSH HL TODO test LD + PUSH with  AF
                                      0xC1           ;; POP BC (gets 0x1234)
                                      ;; 0xE1           ;; POP HL (gets 0xDDEE)
                                      ;; 0xD1           ;; POP DE (gets 0xBBCC)
                                      0x76]})]
    (is (:halted (:cpu ctx)))
    (is (= 0x1234 (r/read-reg ctx :bc))))) ;; TODO getting 0xEE12 (off by one b/t DDEE and 1234)
    ;; (is (= 0xBBCC (r/read-reg ctx :de)))
    ;; (is (= 0xDDEE (r/read-reg ctx :hl)))

(comment
  (format "%04X" 60946)

 nil)
