(ns gbemu.cpu.registers)

(defn read-reg [ctx r]
  (let [regs (get-in ctx [:cpu :registers])]
    (case r
      :af (bit-or (bit-shift-left (regs :a) 8) (regs :f))
      :bc (bit-or (bit-shift-left (regs :b) 8) (regs :c))
      :de (bit-or (bit-shift-left (regs :d) 8) (regs :e))
      :hl (bit-or (bit-shift-left (regs :h) 8) (regs :l))
      (regs r))))


(defn write-reg [ctx r val]
  (let [regs (get-in ctx [:cpu :registers])
        lo   (bit-and 0xFF val)
        hi   (bit-and 0xFF (bit-shift-right val 8))
        changes (case r
                  :af {:a hi :f lo}
                  :bc {:b hi :c lo}
                  :de {:d hi :e lo}
                  :hl {:h hi :l lo}
                  {r lo})]
    (update-in ctx [:cpu :registers] merge changes)))
