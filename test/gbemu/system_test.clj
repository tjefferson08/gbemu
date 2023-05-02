(ns gbemu.system-test
  (:require [gbemu.system :as sut]
            [clojure.test :refer :all]
            [gbemu.bytes :as bytes]
            [gbemu.cpu.registers :as r]
            [gbemu.bus :as bus]
            [gbemu.execution.flags :as flags]
            [gbemu.cpu.core :as cpu]))

(defn submap? [a-map b-map]
 (every? (fn [[k _ :as entry]] (= entry (find b-map k))) a-map))

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
        _            (bytes/spit-bytes rom-file new-rom-bytes)
        ctx          (sut/init rom-file)]
     (cpu/run ctx)))

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
                                      0x10]})]    ;; STOP

    (is (= 0xDFFF (r/read-reg ctx :sp)))

    (is (= 0x1234 (r/read-reg ctx :bc)))
    (is (= 0xDDEE (r/read-reg ctx :hl)))
    (is (= 0xBBCC (r/read-reg ctx :de)))))

(deftest ^:integration load-instructions
  (let [load-1 (ctx-with {:instructions [
                                         0x31 0xFF 0xDF ;; LD SP, 0xDFF0
                                         0x08 0x02 0xD6 ;; LD $(D602), SP
                                         0x3E 0xA1      ;; LD A, 0xA1
                                         0xEA 0x00 0xD6 ;; LD $(D600), $A

                                         0x01 0x01 0xD6 ;; LD BC, $D601
                                         0x02           ;; LD (BC), A
                                         0xEA 0x00 0xD6 ;; LD $(D600), $A
                                         0x10]})]
    (is (= 0xA1 (r/read-reg load-1 :a)))
    (is (= 0xD601 (r/read-reg load-1 :bc)))
    (is (= 0xA1 (bus/read-bus load-1 0xD600)))
    (is (= 0xA1 (bus/read-bus load-1 0xD601)))
    (is (= 0xFF (bus/read-bus load-1 0xD602)))
    (is (= 0xDF (bus/read-bus load-1 0xD603))))

  (let [load-2 (ctx-with {:instructions [0x21 0x82 0xFF ;; LD HL, $FF82
                                         0x36 0x99      ;; LD (HL), $99
                                         0x4E           ;; LD C, (HL)
                                         0x10]})]
    (is (= 0xFF82 (r/read-reg load-2 :hl)))
    (is (= 0x99 (r/read-reg load-2 :c)))
    (is (= 0x99 (bus/read-bus load-2 0xFF82)))))

(deftest ^:integration load-with-special-sp-addressing-mode-opcode-48
  (let [ctx (ctx-with {:instructions [0x21 0x99 0x88 ;; LD HL, $8899
                                      0x31 0x01 0x00 ;; LD SP, 0x0001
                                      0xF8 0x01      ;; LD HL, SP+$01
                                      0x10]})]
    (is (= 0x0002 (r/read-reg ctx :hl)))
    (is (= 0x0001 (r/read-reg ctx :sp)))
    (is (= {:z false, :n false, :h false, :c false} (flags/all ctx))))

  (let [ctx (ctx-with {:instructions [0x21 0x99 0x88 ;; LD HL, $8899
                                      0x31 0x03 0x00 ;; LD SP, 0x0003
                                      0xF8 0xFF      ;; LD HL, SP+$01
                                      0x10]})]
    (is (= 0x0002 (r/read-reg ctx :hl)))
    (is (= 0x0003 (r/read-reg ctx :sp)))
    (is (= {:z false, :n false, :h true, :c true} (flags/all ctx))))

  (let [ctx (ctx-with {:instructions [0x21 0x99 0x88 ;; LD HL, $8899
                                      0x31 0x0F 0x00 ;; LD SP, 0x000F
                                      0xF8 0x01      ;; LD HL, SP+$01
                                      0x10]})]
    (is (= 0x0010 (r/read-reg ctx :hl)))
    (is (= 0x000F (r/read-reg ctx :sp)))
    (is (= {:z false, :n false, :h true, :c false} (flags/all ctx)))))

(deftest ^:integration loadh
  (let [loadh-1 (ctx-with {:instructions [0x3E 0x01      ;; LD A, $01
                                          0xE0 0x01      ;; LDH ($FF01), A
                                          0x3E 0x02      ;; LD A, $02
                                          0xF0 0x01      ;; LDH A, ($FF01)
                                          0x10]})]
    (is (= 0x01 (r/read-reg loadh-1 :a)))
    (is (= 0x01 (bus/read-bus loadh-1 0xFF01)))))

(deftest ^:integration load-and-inc-instructions
  (let [load-hl+ (ctx-with {:instructions [0x3E 0x01      ;; LD A, $01
                                           0x06 0x00      ;; LD B, $00
                                           0x21 0x80 0xFF ;; LD HL, $FF80
                                           0x22           ;; LD (HL+), A
                                           0x3E 0x11      ;; LD A, $11
                                           0x22           ;; LD (HL+), A
                                           0x3E 0x21      ;; LD A, $21
                                           0x32           ;; LD (HL-), A
                                           0x2A           ;; LD A, (HL+)
                                           0x80           ;; ADD A,B
                                           0x47           ;; LD B,A
                                           0x3A           ;; LD A, (HL-)
                                           0x80           ;; ADD A,B
                                           0x47           ;; LD B,A
                                           0x3A           ;; LD A, (HL-)
                                           0x80           ;; ADD A,B
                                           0x47           ;; LD B,A
                                           0x3A           ;; LD A, (HL-)
                                           0x80           ;; ADD A,B
                                           0x47           ;; LD B,A
                                           0x10]})]
    (is (= 0xFF7F (r/read-reg load-hl+ :hl)))
    (is (= (+ 0x01 0x11 0x11 0x21) (r/read-reg load-hl+ :b)))
    (is (= 0x00 (bus/read-bus load-hl+ 0xFF7F)))
    (is (= 0x01 (bus/read-bus load-hl+ 0xFF80)))
    (is (= 0x11 (bus/read-bus load-hl+ 0xFF81)))
    (is (= 0x21 (bus/read-bus load-hl+ 0xFF82)))
    (is (= 0x00 (bus/read-bus load-hl+ 0xFF83)))))

(deftest ^:integration load-and-inc-instructions-2
  (let [load-hl+ (ctx-with {:instructions [0x3E 0x01      ;; LD A, $01
                                           0x21 0x80 0xFF ;; LD HL, $FF80
                                           0x22           ;; LD (HL+), A
                                           0x3E 0x11      ;; LD A, $11
                                           0x22           ;; LD (HL+), A
                                           0x3E 0x21      ;; LD A, $21
                                           0x32           ;; LD (HL-), A
                                           0x10]})]
    (is (= 0xFF81 (r/read-reg load-hl+ :hl)))
    (is (= 0x01 (bus/read-bus load-hl+ 0xFF80)))
    (is (= 0x11 (bus/read-bus load-hl+ 0xFF81)))
    (is (= 0x21 (bus/read-bus load-hl+ 0xFF82)))))


(deftest ^:integration reti
  (let [ctx (ctx-with {:instructions [
                                      0x31 0xFF 0xDF ;; LD SP, 0xDFFF
                                      0x3E 0x01      ;; LD A, 0x01
                                      0xCD 0x00 0x10 ;; CALL $1000 (always)
                                      0x10]
                       :regions {0x1000 [0x3E 0x02      ;; LD A, 0x02
                                         0xD9]}})]          ;; RETI
    (is (= 0x02 (r/read-reg ctx :a)))
    (is (get-in ctx [:cpu :int-master-enabled]))))

(deftest ^:integration jump-instructions
  (let [ctx-jump-1 (ctx-with {:instructions [
                                             0x31 0xFF 0xDF ;; LD SP, 0xDFFF
                                             0x3E 0x01      ;; LD A, 0x01
                                             0xC3 0x00 0x10 ;; JP 0x1000
                                             0x10]
                              :regions {0x1000 [
                                                0x3E 0x02      ;; LD A, 0x02
                                                0xCD 0x00 0x11 ;; CALL 0x1100 (always)
                                                0x06 0x03      ;; LD B, 0x03
                                                0x10]
                                        0x1100 [
                                                0x0E 0x04 ;; LD C 0x04
                                                0xC9]}})] ;; RET (always)]}})]
    (is (= 0x1008 (r/read-reg ctx-jump-1 :pc)))
    (is (= 0x02 (r/read-reg ctx-jump-1 :a)))
    (is (= 0x03 (r/read-reg ctx-jump-1 :b)))
    (is (= 0x04 (r/read-reg ctx-jump-1 :c))))

  (let [ctx-jump-2 (ctx-with {:instructions [
                                             0x31 0xFF 0xDF ;; LD SP, 0xDFFF
                                             0x3E 0x01      ;; LD A, 0x01
                                             0xC3 0x05 0x10] ;; JP 0x1005 (middle of next region)

                              :regions {0x1000 [0x3E 0x02      ;; LD A, 0x02
                                                0xC3 0x00 0x11 ;; JP 0x1100
                                                0x18 0xF9]      ;; JR -7

                                        0x1100 [0x10]}})] ;; STOP
    (is (= 0x02 (r/read-reg ctx-jump-2 :a))))

  (let [ctx-jump-3 (ctx-with {:instructions [0x21 0x01 0x10 ;; LD HL, 0xC001
                                             0x3E 0x01      ;; LD A, 0x01
                                             0xE9]          ;; JP $(HL)

                              :regions {0x1001 [0x3E 0x02      ;; LD A, 0x02
                                                0x10]}})]
    (is (= 0x02 (r/read-reg ctx-jump-3 :a)))))

(deftest ^:integration sbc
  (let [ctx (ctx-with {:instructions [0x3E 0x00    ;; LD A, 0x00
                                      0x06 0x01    ;; LD B, 0x01
                                      0xC6 0x00    ;; ADD A, 0x0 (reset carry)
                                      0x98         ;; SBC A, B
                                      0x10]})]
    (is (= 0xFF (r/read-reg ctx :a)))
    (is (= {:z false, :n true, :h true, :c true} (flags/all ctx)))))

(deftest ^:integration add
  (let [ctx (ctx-with {:instructions [0x31 0xFF 0x00 ;; LD SP, $00FF
                                      0xE8 0x01      ;; ADD SP, $01
                                      0x10]})]
    (is (= 0x0100 (r/read-reg ctx :sp)))
    (is (= {:z false, :n false, :h true, :c true} (flags/all ctx))))

  (let [ctx (ctx-with {:instructions [0x31 0x00 0x01 ;; LD SP, $0100
                                      0xE8 0x01      ;; ADD SP, $01
                                      0x10]})]
    (is (= 0x0101 (r/read-reg ctx :sp)))
    (is (= {:z false, :n false, :h false, :c false} (flags/all ctx)))))

(deftest ^:integration math-instructions
  (let [ctx-inc-1 (ctx-with {:instructions [
                                            0x0E 0x04      ;; LD C, 0x04
                                            0x0C           ;; INC C
                                            0x10]})]
    (is (= 0x05 (r/read-reg ctx-inc-1 :c))))

  (let [ctx-inc-2 (ctx-with {:instructions [0x21 0x01 0xC0 ;; LD HL, 0xC001
                                            0x34           ;; INC (HL)
                                            0x34           ;; INC (HL)
                                            0x35           ;; DEC (HL)
                                            0x10]})]
    (is (= 0x01 (bus/read-bus ctx-inc-2 0xC001))))

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
                                                 0x10]})]
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
                                                0x10]})]
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
                                            0x10]})]
    (is (= 0x82 (bus/read-bus ctx-logic 0xC000)))
    (is (= 0xC6 (bus/read-bus ctx-logic 0xC001)))
    (is (= 0x6C (bus/read-bus ctx-logic 0xC002)))
    (is (= 0x6C (bus/read-bus ctx-logic 0xC003)))
    (let [f-after-cp (bus/read-bus ctx-logic 0xD001)]
      (is (not (flags/flag-set? f-after-cp :z)))
      (is (flags/flag-set? f-after-cp :c))
      (is (flags/flag-set? f-after-cp :n))
      (is (not (flags/flag-set? f-after-cp :h)))))

  (let [ctx-logic-2 (ctx-with {:instructions [0x21 0x80 0xFF ;; LD HL, $FF80
                                              0x36 0xDE      ;; LD (HL), $DE
                                              0x3E 0x31      ;; LD A, $31
                                              0xAE           ;; XOR (HL)
                                              0x10]})]
    (is (= 0xDE (bus/read-bus ctx-logic-2 0xFF80)))
    (is (= 0xEF (r/read-reg ctx-logic-2 :a)))))

(deftest ^:integration rotate-shift-instructions
  (let [ctx-rlc-1 (ctx-with {:instructions [0x06 0xC6      ;; LD B, 0xC6 (0b1100_0110)
                                            0xCB 0x00      ;; RLC B B=0x8D
                                            0x78           ;; LD A,B
                                            0xEA 0x00 0xC0 ;; LD $(0xC000),A
                                            0x10]})]
    (is (= 0x8D (bus/read-bus ctx-rlc-1 0xC000)))
    (is (= {:z false, :n false :h false, :c true} (flags/all ctx-rlc-1))))

  (let [ctx-rrc-1 (ctx-with {:instructions [0x0E 0xC7      ;; LD C, 0xC7 (0b1100_0111)
                                            0xCB 0x09      ;; RRC C C=0xE3
                                            0x79           ;; LD A,C
                                            0xEA 0x00 0xC0 ;; LD $(0xC000),A
                                            0x10]})]
    (is (= 0xE3 (bus/read-bus ctx-rrc-1 0xC000)))
    (is (= {:z false, :n false :h false, :c true} (flags/all ctx-rrc-1))))

  (let [ctx-rl-1 (ctx-with {:instructions [0x3E 0xC7      ;; LD A, 0xC7 (0b1100_0111)
                                           0xC6 0x00      ;; ADD A, 0x0 (reset carry)
                                           0x57           ;; LD D,A
                                           0xCB 0x12      ;; RL D D=0x8E
                                           0x7A           ;; LD A,B
                                           0xEA 0x00 0xC0 ;; LD $(0xC000),A
                                           0x10]})]
    (is (= 0x8E (bus/read-bus ctx-rl-1 0xC000)))
    (is (= {:z false, :n false :h false, :c true} (flags/all ctx-rl-1))))

  (let [ctx-rr-1 (ctx-with {:instructions [0x3E 0xC7      ;; LD A, 0xC7 (0b1100_0111)
                                           0xC6 0x00      ;; ADD A, 0x0 (reset carry)
                                           0x5F           ;; LD E,A
                                           0xCB 0x1B      ;; RR E D=0x63
                                           0x7B           ;; LD A,E
                                           0xEA 0x00 0xC0 ;; LD $(0xC000),A
                                           0x10]})]
    (is (= 0x63 (bus/read-bus ctx-rr-1 0xC000)))
    (is (= {:z false, :n false :h false, :c true} (flags/all ctx-rr-1))))

  (let [ctx-sla-1 (ctx-with {:instructions [0x26 0xC7      ;; LD H, 0xC7 (0b1100_0111)
                                            0xCB 0x24      ;; SLA H H=0x8E
                                            0x7C           ;; LD A,H
                                            0xEA 0x00 0xC0 ;; LD $(0xC000),A
                                            0x10]})]
    (is (= 0x8E (bus/read-bus ctx-sla-1 0xC000)))
    (is (= {:z false, :n false :h false, :c true} (flags/all ctx-sla-1))))

  (let [ctx-sra-1 (ctx-with {:instructions [0x2E 0xC7      ;; LD H, 0xC7 (0b1100_0111)
                                            0xCB 0x2D      ;; SLA L L=0xE3
                                            0x7D           ;; LD A,L
                                            0xEA 0x00 0xC0 ;; LD $(0xC000),A
                                            0x10]})]
    (is (= 0xE3 (bus/read-bus ctx-sra-1 0xC000)))
    (is (= {:z false, :n false :h false, :c true} (flags/all ctx-sra-1))))

 (let [ctx-swap-1 (ctx-with {:instructions [0x21 0x00 0xC0 ;; LD HL, 0xC000
                                            0x36 0xA6      ;; LD $(HL), 0xA6
                                            0xCB 0x36      ;; SWAP $(HL) $(HL)=0x6A
                                            0x10]})]
   (is (= 0x6A (bus/read-bus ctx-swap-1 0xC000)))
   (is (= {:z false, :n false :h false, :c false} (flags/all ctx-swap-1))))

 (let [ctx-srl-1 (ctx-with {:instructions [0x3E 0xC2      ;; LD A, 0xC2   (0b1100_0011)
                                           0xCB 0x3F      ;; SRL A A=0x61 (0b0110_0001)
                                           0xEA 0x00 0xC0 ;; LD $(0xC000),A
                                           0x10]})]
   (is (= 0x61 (bus/read-bus ctx-srl-1 0xC000)))
   (is (= {:z false, :n false :h false, :c false} (flags/all ctx-srl-1)))))

(deftest ^:integration bit-instructions
  (let [ctx-bit-1 (ctx-with {:instructions [0x06 0xC6      ;; LD B, 0xC6 (0b1100_0110)
                                            0xCB 0x40      ;; BIT 0, B
                                            0x10]})

        ctx-bit-2 (ctx-with {:instructions [0x3E 0xC6      ;; LD A, 0xC6 (0b1100_0161)
                                            0xCB 0x4F      ;; BIT 1,A
                                            0x10]})]
   (is (submap? {:z true, :n false :h true} (flags/all ctx-bit-1)))
   (is (submap? {:z false, :n false :h true} (flags/all ctx-bit-2))))

  (let [ctx-res-1 (ctx-with {:instructions [0x21 0x00 0xC0 ;; LD HL, 0xC000
                                            0x36 0xE6      ;; LD $(HL), 0xE6 = 2r11100110
                                            0xCB 0xB6      ;; RES 6,$(HL) $(HL)=0xA6 2r10100110
                                            0x10]})]
    (is (= 0xA6 (bus/read-bus ctx-res-1 0xC000))))

  (let [ctx-set-1 (ctx-with {:instructions [0x21 0x00 0xC0 ;; LD HL, 0xC000
                                            0x36 0xE6      ;; LD $(HL), 0xE6 = 2r11100110
                                            0xCB 0xE6      ;; RES 6,$(HL) $(HL)=0xA6 2r10100110
                                            0x10]})]
    (is (= 0xF6 (bus/read-bus ctx-set-1 0xC000)))))

(deftest ^:integration other-rotate-instructions
  (let [ctx-rlca-1 (ctx-with {:instructions [0x3E 0xC6   ;; LD A, 0xC6 (0b1100_0110)
                                             0xC6 0x00   ;; ADD A, 0x0 (reset carry)
                                             0x07        ;; RLCA
                                             0x10]})

        ctx-rrca-1 (ctx-with {:instructions [0x3E 0xC6   ;; LD A, 0xC6 (0b1100_0110)
                                             0xC6 0x00   ;; ADD A, 0x0 (reset carry)
                                             0x0F        ;; RRCA
                                             0x10]})

        ctx-rla-1 (ctx-with {:instructions [0x3E 0xC6   ;; LD A, 0xC6 (0b1100_0110)
                                            0xC6 0x00   ;; ADD A, 0x0 (reset carry)
                                            0x17        ;; RLA
                                            0x10]})

        ctx-rra-1 (ctx-with {:instructions [0x3E 0xC6   ;; LD A, 0xC6 (0b1100_0110)
                                            0xC6 0x00   ;; ADD A, 0x0 (reset carry)
                                            0x1F        ;; RRA
                                            0x10]})]

    (is (= 0x8D (r/read-reg ctx-rlca-1 :a)))
    (is (= 0x63 (r/read-reg ctx-rrca-1 :a)))
    (is (= 0x8C (r/read-reg ctx-rla-1 :a)))
    (is (= 0x63 (r/read-reg ctx-rra-1 :a)))))

(deftest ^:integration daa
  (let [ctx-daa-1 (ctx-with {:instructions [0x3E 0xCA   ;; LD A, 0xCA (0b1100_1010)
                                            0x27        ;; DAA 2r0010_0111
                                            0x10]})]

    ;; 0x30 = 0x27 | 0x60
    (is (= 0x30 (r/read-reg ctx-daa-1 :a)))
    (is (submap? {:z false, :h false, :c true} (flags/all ctx-daa-1)))))

(deftest ^:integration cpl
  (let [ctx-cpl-1 (ctx-with {:instructions [0x3E 0xCA   ;; LD A, 0xCA (0b1100_1010)
                                            0x2F        ;; CPL
                                            0x10]})]
    (is (= 0x35 (r/read-reg ctx-cpl-1 :a)))
    (is (submap? {:n true, :h true} (flags/all ctx-cpl-1)))))

(comment
  (format "%04X" 99)

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
                  0x10]}]
     0x02 (ex 0x02))


  (vec (take 0x200 (repeat 0x0)))
  (bit-test 0x71 7)

  (assoc [0 1] 1 9)
  0x10


 nil)
