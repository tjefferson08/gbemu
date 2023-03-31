(ns gbemu.cpu.registers)

(defn read-reg [ctx r]
  (let [regs (get-in ctx [:cpu :registers])]
    (case r
      :af (bit-or (bit-shift-left (regs :a) 8) (regs :f))
      :bc (bit-or (bit-shift-left (regs :b) 8) (regs :c))
      :de (bit-or (bit-shift-left (regs :d) 8) (regs :e))
      :hl (bit-or (bit-shift-left (regs :h) 8) (regs :l))
      (regs r))))
