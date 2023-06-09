(ns gbemu.execution.math
  (:require [gbemu.cpu.registers :as r]
            [gbemu.bus :as bus]
            [gbemu.bytes :as bytes]
            [gbemu.execution.flags :as flags]
            [gbemu.clock :as clock]))

(defn increment [ctx]
  (let [cur-instr                    (get-in ctx [:cpu :cur-instr])
        cur-opcode                   (get-in ctx [:cpu :cur-opcode])
        {:keys [reg1 mode] :as inst} cur-instr
        inc-mem  (fn [ctx]
                   (let [addr        (r/read-reg ctx reg1)
                         [v ctx']    (bus/read ctx addr)
                         v'          (bytes/to-unsigned (inc v))]
                    [v' (bus/write-bus ctx' addr v')]))
        inc-reg  (fn [ctx]
                    (let [v    (inc (r/read-reg ctx reg1))
                          ctx' (r/write-reg ctx reg1 v)
                          v'   (r/read-reg ctx' reg1)]
                      [v' ctx']))
        [v ctx'] (if (= mode :memloc) (inc-mem ctx) (inc-reg ctx))
        ctx''    (if (r/sixteen-bit? reg1) (clock/tick ctx' 4) ctx')]
    (if (= 0x03 (bit-and 0x03 cur-opcode))
      ctx''
      (flags/set-flags ctx'' {:z (zero? v), :n false, :h (zero? (bit-and 0x0F v))}))))

(defn decrement [ctx]
  (let [cur-instr                    (get-in ctx [:cpu :cur-instr])
        cur-opcode                   (get-in ctx [:cpu :cur-opcode])
        {:keys [reg1 mode] :as inst} cur-instr
        dec-mem (fn [ctx]
                  (let [addr       (r/read-reg ctx reg1)
                        [v ctx']   (bus/read ctx addr)
                        v'         (bytes/to-unsigned (dec v))]
                   [v' (bus/write-bus ctx' addr v')]))
        dec-reg (fn [ctx]
                   (let [v    (dec (r/read-reg ctx reg1))
                         ctx' (r/write-reg ctx reg1 v)
                         v'   (r/read-reg ctx' reg1)]
                     [v' ctx']))
        [v ctx'] (if (= mode :memloc) (dec-mem ctx) (dec-reg ctx))
        ctx''    (if (r/sixteen-bit? reg1) (clock/tick ctx' 4) ctx')]
    (if (= 0x0B (bit-and 0x0B cur-opcode))
      ctx''
      (flags/set-flags ctx'' {:z (zero? v), :n true, :h (= 0x0F (bit-and 0x0F v))}))))


;; 16 bit reg but 8 bit operands
(defn- add-sp [ctx]
  (let [sp                               (r/read-reg ctx :sp)
        {:keys [cur-instr fetched-data]} (ctx :cpu)
        {:keys [reg1] :as inst}          cur-instr
        operand                          (bytes/extend-sign fetched-data)
        v                                (+ sp operand)
        nibble-sum                       (bytes/mask-sum 0x0F fetched-data sp)
        byte-sum                         (bytes/mask-sum 0xFF fetched-data sp)
        h                                (<= 0x10 nibble-sum)
        c                                (< 0xFF byte-sum)
        v'                               (bit-and 0xFFFF v)]
    (-> ctx
      (r/write-reg :sp v')
      (flags/set-flags {:z false, :n false, :h h, :c c}))))

(defn- add-16-bit [ctx]
  (let [{:keys [cur-instr fetched-data]} (ctx :cpu)
        {:keys [reg1] :as inst}          cur-instr]
    (if (= :sp reg1)
      (add-sp ctx)
      (let [operand                          (r/read-reg ctx reg1)
            v                                (+ fetched-data operand)
            half-sum                         (+ (bit-and 0x0FFF fetched-data) (bit-and 0x0FFF operand))
            h                                (<= 0x1000 half-sum)
            c                                (< 0xFFFF v)
            ;; _ (println (ctx :cpu))
            v'                               (bit-and 0xFFFF v)]
        (-> ctx
          (r/write-reg reg1 v')
          (flags/set-flags {:n false, :h h, :c c}))))))

(defn- add-8-bit [ctx]
  (let [{:keys [cur-instr fetched-data]} (ctx :cpu)
        {:keys [reg1 reg2] :as inst}     cur-instr
        operand                          (r/read-reg ctx reg1)
        v                                (+ fetched-data operand)
        half-sum                         (bytes/half-sum operand fetched-data)
        h                                (<= 0x10 half-sum)
        c                                (< 0xFF v)
        ;; _ (println "carry?" c)
        v'                               (bytes/to-unsigned v)]
    (-> ctx
        (r/write-reg reg1 v')
        (flags/set-flags {:z (zero? v'), :n false, :h h, :c c}))))

(defn add [ctx]
  (let [reg1 (get-in ctx [:cpu :cur-instr :reg1])]
    (if (r/sixteen-bit? reg1)
      (clock/tick (add-16-bit ctx) 4)
      (add-8-bit ctx))))

(defn adc [{{:keys [cur-instr fetched-data]} :cpu :as ctx}]
  (let [{:keys [reg1 reg2] :as inst}  cur-instr
        operand                       (r/read-reg ctx reg1)
        c                             (if (flags/flag-set? ctx :c) 1 0)
        v                             (+ fetched-data operand c)
        v'                            (bit-and v 0xFF)
        z                             (zero? v')
        half-sum                      (+ c (bytes/half-sum operand fetched-data))
        h                             (< 0xF half-sum)
        c'                            (< 0xFF v)]
    (-> ctx
        (r/write-reg reg1 v')
        (flags/set-flags {:z z, :n false, :h h, :c c'}))))

(defn sub [{{:keys [cur-instr fetched-data]} :cpu :as ctx}]
  (let [{:keys [reg1] :as inst}          cur-instr
        operand                          (r/read-reg ctx reg1)
        v                                (- operand fetched-data)
        h                                (neg? (bytes/half-diff operand fetched-data))
        c                                (neg? v)
        v'                               (bytes/to-unsigned v)]
    (-> ctx
        (r/write-reg reg1 v')
        (flags/set-flags {:z (zero? v'), :n true, :h h, :c c}))))

(defn sbc [{{:keys [cur-instr fetched-data]} :cpu :as ctx}]
  (let [{:keys [reg1] :as inst}          cur-instr
        operand                          (r/read-reg ctx reg1)
        c                                (if (flags/flag-set? ctx :c) 1 0)
        v                                (- operand fetched-data c)
        h                                (neg? (- (bytes/half-diff operand fetched-data) c))
        c'                               (neg? v)
        v'                               (bytes/to-unsigned v)]
    (-> ctx
        (r/write-reg reg1 v')
        (flags/set-flags {:z (zero? v'), :h h, :n true, :c c'}))))



(comment

 ,)
