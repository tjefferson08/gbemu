(ns gbemu.execution.cb
  (:require [gbemu.cpu.registers :as r]
            [gbemu.bus :as bus]
            [gbemu.execution.flags :as flags]))

(defn- read-for [ctx op]
  (let [low-3-bits (bit-and 2r0111 op)]
    (case low-3-bits
      0x0 [(r/read-reg ctx :b) ctx]
      0x1 [(r/read-reg ctx :c) ctx]
      0x2 [(r/read-reg ctx :d) ctx]
      0x3 [(r/read-reg ctx :e) ctx]
      0x4 [(r/read-reg ctx :h) ctx]
      0x5 [(r/read-reg ctx :l) ctx]
      0x6 (bus/read ctx (r/read-reg ctx :hl))
      0x7 [(r/read-reg ctx :a) ctx])))

(defn- write-for [ctx op val]
  (let [low-3-bits (bit-and 2r0111 op)]
    (case low-3-bits
     0x0 (r/write-reg ctx :b val)
     0x1 (r/write-reg ctx :c val)
     0x2 (r/write-reg ctx :d val)
     0x3 (r/write-reg ctx :e val)
     0x4 (r/write-reg ctx :h val)
     0x5 (r/write-reg ctx :l val)
     0x6 (bus/write-bus ctx (r/read-reg ctx :hl) val)
     0x7 (r/write-reg ctx :a val))))

;; TODO use multimethod to help vary dispatch for bit ops vs. rotate/shift?
(defn- rotate-shift-op-for [op]
  (let [op-bits (bit-and 2r111 (bit-shift-right op 3))]
    (case op-bits
      0 (fn rlc [ctx v]
          (let [c   (bit-test v 7)
                v'  (bit-and 0xFF (bit-shift-left v 1))
                v'' (if c (bit-set v' 0) v')]
            (-> ctx (write-for op v'')
                    (flags/set-flags {:z (zero? v''), :n false, :h false, :c c}))))
     1 (fn rrc [ctx v]
         (let [c   (bit-test v 0)
               v'  (bit-and 0xFF (bit-shift-right v 1))
               v'' (if c (bit-set v' 7) v')]
           (-> ctx (write-for op v'')
                   (flags/set-flags {:z (zero? v''), :n false, :h false, :c c}))))

     2 (fn rl [ctx v]
         (let [c   (flags/flag-set? ctx :c)
               c'  (bit-test v 7)
               v'  (bit-and 0xFF (bit-shift-left v 1))
               v'' (if c (bit-set v' 0) v')]
           (-> ctx (write-for op v'')
                   (flags/set-flags {:z (zero? v''), :n false, :h false, :c c'}))))

     3 (fn rr [ctx v]
         (let [c   (flags/flag-set? ctx :c)
               c'  (bit-test v 0)
               v'  (bit-and 0xFF (bit-shift-right v 1))
               v'' (if c (bit-set v' 7) v')]
           (-> ctx (write-for op v'')
                   (flags/set-flags {:z (zero? v''), :n false, :h false, :c c'}))))

     4 (fn sla [ctx v]
         (let [c   (bit-test v 7)
               v'  (bit-and 0xFF (bit-shift-left v 1))]
           (-> ctx (write-for op v')
                   (flags/set-flags {:z (zero? v'), :n false, :h false, :c c}))))

     5 (fn sra [ctx v]
         (let [c         (bit-test v 0)
               negative? (bit-test v 7)
               v'        (bit-and 0xFF (bit-shift-right v 1))
               v''       (if negative? (bit-set v' 7) v')]
           (-> ctx (write-for op v'')
                   (flags/set-flags {:z (zero? v''), :n false, :h false, :c c}))))

     6 (fn swap [ctx v]
         (let [lo  (bit-and 0x0F v)
               hi  (bit-and 0xF0 v)
               lo' (bit-shift-right hi 4)
               hi' (bit-shift-left lo 4)
               v'  (bit-or hi' lo')]
           (-> ctx (write-for op v')
                   (flags/set-flags {:z (zero? v'), :n false, :h false, :c false}))))

     7 (fn sra [ctx v]
         (let [c         (bit-test v 0)
               v'        (bit-and 0xFF (bit-shift-right v 1))]
           (-> ctx (write-for op v')
                   (flags/set-flags {:z (zero? v'), :n false, :h false, :c c})))))))

(defn bit-op-for [op]
  (let [op-bits (bit-and 2r11 (bit-shift-right op 6))
        bit-n   (bit-and 2r111 (bit-shift-right op 3))]
    (case op-bits
      1 (fn bit [ctx v]
          (let [mask (bit-set 0 bit-n)
                v'   (bit-and v mask)]
            (flags/set-flags ctx {:z (zero? v'), :n false, :h true})))
      2 (fn res [ctx v]
          (let [mask (bit-clear 0xFF bit-n)
                v'   (bit-and v mask)]
            (-> ctx (write-for op v'))))
      3 (fn set [ctx v]
          (let [mask (bit-set 0 bit-n)
                v'   (bit-or v mask)]
            (-> ctx (write-for op v')))))))

(defn cb-prefix [{{:keys [cur-instr fetched-data]} :cpu :as ctx}]
  (let [op         fetched-data
        [in ctx'] (read-for ctx op)
        handler    (if (< op 0x40) (rotate-shift-op-for op) (bit-op-for op))
        ctx'' (handler ctx' in)]
    ctx''))

(comment
  (format "%02X" (bit-and 2r111 (bit-shift-right 0xB0 3)))

 ,)
