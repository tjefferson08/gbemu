(ns gbemu.execution.cb
  (:require [gbemu.cpu.registers :as r]
            [gbemu.bus :as bus]
            [gbemu.execution.flags :as flags]))

(defn- read-for [ctx op]
  (let [low-3-bits (bit-and 2r0111 op)]
    (case low-3-bits
      0x0 (r/read-reg ctx :b)
      0x1 (r/read-reg ctx :c)
      0x2 (r/read-reg ctx :d)
      0x3 (r/read-reg ctx :e)
      0x4 (r/read-reg ctx :h)
      0x5 (r/read-reg ctx :l)
      0x6 (bus/read-bus ctx (r/read-reg ctx :hl)) ;; TODO cycles
      0x7 (r/read-reg ctx :b))))

(defn- write-for [ctx op val]
  (case op
   0x0 (r/write-reg ctx :b val)
   0x1 (r/write-reg ctx :c val)
   0x2 (r/write-reg ctx :d val)
   0x3 (r/write-reg ctx :e val)
   0x4 (r/write-reg ctx :h val)
   0x5 (r/write-reg ctx :l val)
   0x6 (bus/write-bus ctx val) ;; TODO cycles
   0x7 (r/write-reg ctx :b val)))

;; TODO use multimethod to help vary dispatch for bit ops vs. rotate/shift?
(defn- rotate-shift-op-for [op]
  (let [op-bits (bit-and 2r011 (bit-shift-right op 3))]
    (case op
      0 (fn rlc [ctx v]
          (let [c   (bit-test v 7)
                v'  (bit-and 0xFF (bit-shift-left v 1))
                v'' (if c (bit-or v' 1) v')]
            (-> ctx (write-for op v'')
                    (flags/set-flags {:z (zero? v''), :n false, :h false, :c c}))))

     1 (fn rrc [ctx] ctx))))


(defn cb-prefix [{{:keys [cur-instr fetched-data]} :cpu :as ctx}]
  (let [op fetched-data
        in (read-for ctx op)
        handler (rotate-shift-op-for op)
        ctx' (handler ctx in)]
    ctx'))

(comment
  (format "%02X" 112)

 ,)
