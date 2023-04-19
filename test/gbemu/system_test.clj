(ns gbemu.system-test
  (:require [gbemu.system :as sut]
            [clojure.test :refer :all]
            [gbemu.bytes :as bytes]
            [gbemu.cpu.registers :as r]
            [gbemu.bus :as bus]
            [gbemu.execution.flags :as flags]))

(defn build-rom-vec [instructions regions]
  (let [header-bytes (bytes/slurp-bytes "resources/roms/header-only.gb")
        base-rom     (vec (take 0x8000 (concat header-bytes instructions (repeat 0x00))))
        write-region (fn [rom start instructions]
                       (reduce
                        (fn [arr [idx instr]] (assoc arr (+ start idx) instr))
                        rom
                        (map-indexed vector instructions)))]
   (reduce (fn [arr [start instructions]] (write-region arr start instructions)) base-rom regions)))

(defn ctx-with [{:keys [instructions regions]}]
  (let [rom-file     (clojure.java.io/file "/tmp/tempfile-rom.gb")
        new-rom-bytes (byte-array (build-rom-vec instructions regions))
        _            (bytes/spit-bytes rom-file new-rom-bytes)]
     (sut/boot rom-file)))

(deftest ^:integration stack-operations
  (let [ctx (ctx-with {:instructions [
                                      0x31 0xFF 0xDF ;; LD SP, 0xDFFF
                                      0x01 0xCC 0xBB ;; LD BC, 0xBBCC
                                      0xC5           ;; PUSH BC
                                      0x11 0xEE 0xDD ;; LD DE, 0xDDEE
                                      0xD5           ;; PUSH DE
                                      0x21 0x34 0x12 ;; LD HL, 0x1234
                                      0xE5           ;; PUSH HL TODO test LD + PUSH with  AF
                                      0xC1           ;; POP BC (gets 0x1234)
                                      0xE1           ;; POP HL (gets 0xDDEE)
                                      0xD1           ;; POP DE (gets 0xBBCC)
                                      0x76]})]
    (is (:halted (:cpu ctx)))

    (is (= 0xDFFF (r/read-reg ctx :sp)))

    (is (= 0x1234 (r/read-reg ctx :bc)))
    (is (= 0xDDEE (r/read-reg ctx :hl)))
    (is (= 0xBBCC (r/read-reg ctx :de)))))

(deftest ^:integration load-instructions
  (let [ctx (ctx-with {:instructions [
                                      0x31 0xFF 0xDF ;; LD SP, 0xDFFF
                                      0x3E 0xA1      ;; LD A, 0xA1
                                      0xEA 0x00 0xD6 ;; LD $(D600), $A
                                      0x76]})]
    (is (:halted (:cpu ctx)))
    (is (= 0xDFFF (r/read-reg ctx :sp)))
    (is (= 0xA1 (r/read-reg ctx :a)))
    (is (= 0xA1 (bus/read-bus ctx 0xD600)))))

(deftest ^:integration jump-instructions
  (let [ctx (ctx-with {:instructions [
                                      0x31 0xFF 0xDF ;; LD SP, 0xDFFF
                                      0x3E 0x01      ;; LD A, 0x01
                                      0xC3 0x00 0x10 ;; JP 0x1000
                                      0x76]
                       :regions {0x1000 [
                                         0x3E 0x02      ;; LD A, 0x02
                                         0xCD 0x00 0x11 ;; CALL 0x1100 (always)
                                         0x06 0x03      ;; LD B, 0x03
                                         0x76]
                                 0x1100 [
                                         0x0E 0x04 ;; LD C 0x04
                                         0xC9]}})] ;; RET (always)]}})]


    (is (:halted (:cpu ctx)))
    (is (= 0x1008 (r/read-reg ctx :pc)))
    (is (= 0x02 (r/read-reg ctx :a)))
    (is (= 0x03 (r/read-reg ctx :b)))
    (is (= 0x04 (r/read-reg ctx :c)))))

(deftest ^:integration math-instructions
  (let [ctx-16-bit-add (ctx-with {:instructions [
                                                 0x31 0xAA 0x00 ;; LD SP, 0x00AA
                                                 0xE8 0x10      ;; ADD SP 0x10 (16)
                                                 0xE8 0xF1      ;; ADD SP 0xF0 (-15)
                                                 0x21 0x01 0x00 ;; LD, HL 0x0001
                                                 0x29           ;; ADD HL, HL (HL=0x0002)
                                                 0x39           ;; ADD HL, SP (HL=0x00AD)
                                                 0x01 0x00 0x01 ;; LD BC, 0x0100
                                                 0x09           ;; ADD HL, BC (HL=0x01AD)
                                                 0x11 0x00 0x02 ;; LD DE, 0x0200
                                                 0x19           ;; ADD HL, DE (HL=0x03AD)
                                                 0x76]})]
    (is (:halted (:cpu ctx-16-bit-add)))
    (is (= 0x00AB (r/read-reg ctx-16-bit-add :sp)))
    (is (= 0x0100 (r/read-reg ctx-16-bit-add :bc)))
    (is (= 0x03AD (r/read-reg ctx-16-bit-add :hl))))

  (let [ctx-8-bit-add (ctx-with {:instructions [
                                                0x3E 0x01    ;; LD A, 0x01
                                                0x06 0x02    ;; LD B, 0x02
                                                0x80         ;; ADD A,B (A=0x03)
                                                0x0E 0x04    ;; LD C, 0x04
                                                0x81         ;; ADD A,C (A=0x07)
                                                0x16 0x08    ;; LD D, 0x08
                                                0x82         ;; ADD A,D (A=0x0F)
                                                0x1E 0x10    ;; LD E, 0x10
                                                0x83         ;; ADD A,E (A=0x1F)
                                                0x26 0x20    ;; LD H, 0x20
                                                0x84         ;; ADD A,H (A=0x3F)
                                                0x2E 0x01    ;; LD L, 0x40
                                                0x85         ;; ADD A,L (A=0x40)
                                                0x87           ;; ADD A,A (A=0x80)
                                                0x21 0x01 0xC0 ;; LD HL, 0xC001
                                                0x36 0xFA      ;; LD $(HL), 0xFA
                                                0x86           ;; ADD A,$(HL) (A=0x??)
                                                0x76]})]
    (is (= 0xFA (bus/read-bus ctx-8-bit-add 0xC001)))
    (is (= 0x7A (r/read-reg ctx-8-bit-add :a)))
    (is (not (flags/flag-set? ctx-8-bit-add :z)))
    (is (flags/flag-set? ctx-8-bit-add :c))))

(deftest ^:integration logic-instructions
  (let [ctx-logic (ctx-with {:instructions [
                                            0x31 0xFF 0xDF ;; LD SP, 0xDFFF

                                            0x3E 0xAA      ;; LD A, 0xAA     (0b1010_1010)
                                            0x06 0xC6      ;; LD B, 0xC6     (0b1100_0110)
                                            0xA0           ;; AND B (A=0x82) (0b1000_0010)
                                            0xEA 0x00 0xC0 ;; LD $(0xC000),A

                                            0xB0           ;; OR B  (A=0xC6)
                                            0xEA 0x01 0xC0 ;; LD $(0xC001),A

                                            0x3E 0xAA      ;; LD A, 0xAA      (0b1010_1010)
                                            0x06 0xC6      ;; LD B, 0xC6      (0b1100_0110)
                                            0xA8           ;; XOR B (A=0x6C)  (0b0110_1100)
                                            0xEA 0x02 0xC0 ;; LD $(0xC002),A

                                            0xB8           ;; CP B
                                            0xEA 0x03 0xC0 ;; LD $(0xC003),A

                                            ;; Store flags into memory (roundabout via stack)
                                            0xF5           ;; PUSH AF
                                            0xC1           ;; POP BC
                                            0x79           ;; LD A,C
                                            0xEA 0x01 0xD0 ;; LD $(0xD001),A
                                            0x76]})]
    (is (= 0x82 (bus/read-bus ctx-logic 0xC000)))
    (is (= 0xC6 (bus/read-bus ctx-logic 0xC001)))
    (is (= 0x6C (bus/read-bus ctx-logic 0xC002)))
    (is (= 0x6C (bus/read-bus ctx-logic 0xC003)))
    (let [f-after-cp (bus/read-bus ctx-logic 0xD001)]
      (is (not (flags/flag-set? f-after-cp :z)))
      (is (flags/flag-set? f-after-cp :c))
      (is (flags/flag-set? f-after-cp :n))
      (is (not (flags/flag-set? f-after-cp :h))))))

(deftest ^:integration rotate-shift-instructions
  (let [ctx-rlc-1 (ctx-with {:instructions [0x06 0xC6      ;; LD B, 0xC6 (0b1100_0110)
                                            0xCB 0x00      ;; RLC B B=0x8D
                                            0x78           ;; LD A,B
                                            0xEA 0x00 0xC0 ;; LD $(0xC000),A
                                            0x76]})]
    (is (= 0x8D (bus/read-bus ctx-rlc-1 0xC000)))
    (is (= {:z false, :n false :h false, :c true} (flags/all ctx-rlc-1))))

  (let [ctx-rrc-1 (ctx-with {:instructions [0x0E 0xC7      ;; LD C, 0xC7 (0b1100_0111)
                                            0xCB 0x09      ;; RRC C C=0xE3
                                            0x79           ;; LD A,C
                                            0xEA 0x00 0xC0 ;; LD $(0xC000),A
                                            0x76]})]
    (is (= 0xE3 (bus/read-bus ctx-rrc-1 0xC000)))
    (is (= {:z false, :n false :h false, :c true} (flags/all ctx-rrc-1))))

  (let [ctx-rl-1 (ctx-with {:instructions [0x16 0xC7      ;; LD D, 0xC7 (0b1100_0111)
                                           0xCB 0x12      ;; RL D D=0x8E
                                           0x7A           ;; LD A,B
                                           0xEA 0x00 0xC0 ;; LD $(0xC000),A
                                           0x76]})]
    (is (= 0x8E (bus/read-bus ctx-rl-1 0xC000)))
    (is (= {:z false, :n false :h false, :c true} (flags/all ctx-rl-1))))

  (let [ctx-rr-1 (ctx-with {:instructions [0x1E 0xC7      ;; LD E, 0xC7 (0b1100_0111)
                                           0xCB 0x1B      ;; RR E D=0x63
                                           0x7B           ;; LD A,E
                                           0xEA 0x00 0xC0 ;; LD $(0xC000),A
                                           0x76]})]
    (is (= 0x63 (bus/read-bus ctx-rr-1 0xC000)))
    (is (= {:z false, :n false :h false, :c true} (flags/all ctx-rr-1))))

  (let [ctx-sla-1 (ctx-with {:instructions [0x26 0xC7      ;; LD H, 0xC7 (0b1100_0111)
                                            0xCB 0x24      ;; SLA H H=0x8E
                                            0x7C           ;; LD A,H
                                            0xEA 0x00 0xC0 ;; LD $(0xC000),A
                                            0x76]})]
    (is (= 0x8E (bus/read-bus ctx-sla-1 0xC000)))
    (is (= {:z false, :n false :h false, :c true} (flags/all ctx-sla-1))))

  (let [ctx-sra-1 (ctx-with {:instructions [0x2E 0xC7      ;; LD H, 0xC7 (0b1100_0111)
                                            0xCB 0x2D      ;; SLA L L=0xE3
                                            0x7D           ;; LD A,L
                                            0xEA 0x00 0xC0 ;; LD $(0xC000),A
                                            0x76]})]
    (is (= 0xE3 (bus/read-bus ctx-sra-1 0xC000)))
    (is (= {:z false, :n false :h false, :c true} (flags/all ctx-sra-1))))

 (let [ctx-swap-1 (ctx-with {:instructions [0x21 0x00 0xC0 ;; LD HL, 0xC000
                                            0x36 0xA6      ;; LD $(HL), 0xA6
                                            0xCB 0x36      ;; SWAP $(HL) $(HL)=0x6A
                                            0x76]})]
   (is (= 0x6A (bus/read-bus ctx-swap-1 0xC000)))
   (is (= {:z false, :n false :h false, :c false} (flags/all ctx-swap-1))))

 (let [ctx-srl-1 (ctx-with {:instructions [0x3E 0xC2      ;; LD A, 0xC2   (0b1100_0011)
                                           0xCB 0x3F      ;; SRL A A=0x61 (0b0110_0001)
                                           0xEA 0x00 0xC0 ;; LD $(0xC000),A
                                           0x76]})]
   (is (= 0x61 (bus/read-bus ctx-srl-1 0xC000)))
   (is (= {:z false, :n false :h false, :c false} (flags/all ctx-srl-1)))))

(deftest ^:integration bit-instructions
  (let [ctx-bit-1 (ctx-with {:instructions [0x06 0xC6      ;; LD B, 0xC6 (0b1100_0110)
                                            0xCB 0x40      ;; BIT 0, B
                                            0x76]})

        ctx-bit-2 (ctx-with {:instructions [0x3E 0xC6      ;; LD A, 0xC6 (0b1100_0161)
                                            0xCB 0x4F      ;; BIT 1,A
                                            0x76]})]
   (is (= {:z true, :n false :h true, :c false} (flags/all ctx-bit-1)))
   (is (= {:z false, :n false :h true, :c false} (flags/all ctx-bit-2))))

  (let [ctx-res-1 (ctx-with {:instructions [0x21 0x00 0xC0 ;; LD HL, 0xC000
                                            0x36 0xE6      ;; LD $(HL), 0xE6 = 2r11100110
                                            0xCB 0xB6      ;; RES 6,$(HL) $(HL)=0xA6 2r10100110
                                            0x76]})]
    (is (= 0xA6 (bus/read-bus ctx-res-1 0xC000))))

  (let [ctx-set-1 (ctx-with {:instructions [0x21 0x00 0xC0 ;; LD HL, 0xC000
                                            0x36 0xE6      ;; LD $(HL), 0xE6 = 2r11100110
                                            0xCB 0xE6      ;; RES 6,$(HL) $(HL)=0xA6 2r10100110
                                            0x76]})]
    (is (= 0xF6 (bus/read-bus ctx-set-1 0xC000)))))

(comment
  (format "%04X" 194)

  (format "%02X" (bit-xor 0xAA 0xC6))
  (format "%02X" 2r11000110)

  (int 0xF0)

  (unchecked-byte -69)

  (format "%08X" (unchecked-byte -69))

  (format "%04X" (bit-and 0xC3 0x75))

  (aset-byte (byte-array 10) 2 3 4 5)

  (let [rom (vec (take 0x50 (repeat 0x0)))
        ex {0x02 [
                  0x3E 0x02      ;; LD A, 0x02
                  0x76]}]
     0x02 (ex 0x02))


  (vec (take 0x200 (repeat 0x0)))
  (bit-test 0x71 7)

  (assoc [0 1] 1 9)
  0x10


 nil)
