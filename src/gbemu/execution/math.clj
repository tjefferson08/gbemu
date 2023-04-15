(ns gbemu.execution.math
  (:require [gbemu.cpu.registers :as r]
            [gbemu.bus :as bus]
            [gbemu.bytes :as bytes]
            [gbemu.execution.flags :as flags]))

(defn increment [ctx]
  (let [cur-instr                    (get-in ctx [:cpu :cur-instr])
        cur-opcode                   (get-in ctx [:cpu :cur-opcode])
        {:keys [reg1 mode] :as inst} cur-instr
        inc-mem (fn [ctx]
                  (let [addr (r/read-reg ctx reg1)
                        v    (inc (bus/read-bus ctx addr))
                        v'   (bytes/to-unsigned v)]
                   [v' (bus/write-bus ctx addr v')]))
        inc-reg (fn [ctx]
                   (let [v    (inc (r/read-reg ctx reg1))
                         ctx' (r/write-reg ctx reg1 v)
                         v'   (r/read-reg ctx' reg1)]
                     [v' ctx']))
        [v ctx'] (if (= mode :meml) (inc-mem ctx) (inc-reg ctx))]
    (if (= 0x03 (bit-and 0x03 cur-opcode))
      ctx'
      (flags/set-flags ctx {:z (zero? v), :n false, :h (zero? (bit-and 0x0F))}))))

(defn decrement [ctx]
  (let [cur-instr                    (get-in ctx [:cpu :cur-instr])
        cur-opcode                   (get-in ctx [:cpu :cur-opcode])
        {:keys [reg1 mode] :as inst} cur-instr
        dec-mem (fn [ctx]
                  (let [addr (r/read-reg ctx reg1)
                        v    (dec (bus/read-bus ctx addr))
                        v'   (bytes/to-unsigned v)]
                   [v' (bus/write-bus ctx addr v')]))
        dec-reg (fn [ctx]
                   (let [v    (dec (r/read-reg ctx reg1))
                         ctx' (r/write-reg ctx reg1 v)
                         v'   (r/read-reg ctx' reg1)]
                     [v' ctx']))
        [v ctx'] (if (= mode :meml) (dec-mem ctx) (dec-reg ctx))]
    (if (= 0x0B (bit-and 0x0B cur-opcode))
      ctx'
      (flags/set-flags ctx {:z (zero? v), :n true, :h (= 0x0F (bit-and 0x0F))}))))


;; 16 bit reg but 8 bit operands
(defn- add-sp [ctx]
    ;; TODO special signed int handling for SP add
      (let [sp                               (r/read-reg ctx :sp)
            {:keys [cur-instr fetched-data]} (ctx :cpu)
            {:keys [reg1] :as inst}          cur-instr
            operand                          (bytes/extend-sign fetched-data)
            v                                (+ sp operand)
            half-sum                         (+ (bit-and 0x0F fetched-data) (bit-and 0x0F sp))
            h                                (<= 0x10 half-sum)
            c                                (<= 0xFFFF v)
            v'                               (bit-and 0xFFF v)]
        (-> ctx
          (r/write-reg :sp v')
          (flags/set-flags {:z 0, :n 0, :h h, :c c}))))

(defn- add-16-bit [ctx]
  (let [{:keys [cur-instr fetched-data]} (ctx :cpu)
        {:keys [reg1] :as inst}          cur-instr
        a                                (r/read-reg ctx :a)]
    ;; TODO add 1 cycle for 16 bit reg1
    (if (= :sp reg1)
      (add-sp ctx)
      ctx)))

(defn- add-8-bit [ctx]
  ctx)

(defn add [ctx]
  (let [reg1 (get-in ctx [:cpu :cur-instr :reg1])]
    (if (r/sixteen-bit? reg1)
      (add-16-bit ctx)
      (add-8-bit ctx))))
