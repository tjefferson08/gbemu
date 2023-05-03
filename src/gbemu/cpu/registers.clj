(ns gbemu.cpu.registers
  (:require [gbemu.bytes :as bytes]
            [gbemu.log :as log]))

(defn eight-bit? [r]
  (boolean (#{:a :f :b :c :d :e :h :l} r)))

(defn sixteen-bit? [r] (not (eight-bit? r)))

(defn read-reg [ctx r]
  (let [regs (get-in ctx [:cpu :registers])]
    (case r
      :af (bit-or (bit-shift-left (regs :a) 8) (regs :f))
      :bc (bit-or (bit-shift-left (regs :b) 8) (regs :c))
      :de (bit-or (bit-shift-left (regs :d) 8) (regs :e))
      :hl (bit-or (bit-shift-left (regs :h) 8) (regs :l))
      (regs r))))

(defn read-regs [ctx & rs]
  (map #(read-reg ctx %) rs))

(defn read-interrupt-flags [ctx]
  (get-in ctx [:cpu :int-flags]))

(defn write-interrupt-flags [ctx value]
  (log/stderr (str "writing int flags: " value))
  (assoc-in ctx [:cpu :int-flags] (bytes/to-u8 value)))

(defn read-ie-reg [ctx]
  (get-in ctx [:cpu :ie-register]))

(defn write-ie-reg [ctx value]
  (assoc-in ctx [:cpu :ie-register] (bytes/to-u8 value)))

(defn write-reg [ctx r val]
  (let [regs (get-in ctx [:cpu :registers])
        lo   (bit-and 0xFF val)
        hi   (bit-and 0xFF (bit-shift-right val 8))
        u16  (bytes/to-u16 val)
        changes (case r
                  :af {:a hi :f lo}
                  :bc {:b hi :c lo}
                  :de {:d hi :e lo}
                  :hl {:h hi :l lo}
                  :sp {:sp u16}
                  :pc {:pc u16}
                  {r lo})]
    (update-in ctx [:cpu :registers] merge changes)))

(comment
  (boolean nil)

  nil)
