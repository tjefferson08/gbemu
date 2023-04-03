(ns gbemu.instruction
  (:require [gbemu.bytes :as b]))

(def instructions
  {0x00 {:type :no-op, :mode :implied}
   0x01 {:type :load, :mode :register_d16, :reg1 :bc}
   0x02 {:type :load, :mode :memloc_register, :reg1 :bc, :reg2 :a}
   0x05 {:type :decrement, :mode :register, :reg1 :b}
   0x06 {:type :load, :mode :register_d8, :reg1 :b}
   0x08 {:type :load, :mode :a16_register, :reg2 :sp}
   0x0A {:type :load, :mode :register_memloc, :reg1 :a, :reg2 :bc}
   0x0E {:type :load, :mode :register_d8, :reg1 :c}

   0x11 {:type :load, :mode :register_d16, :reg1 :de}
   0x12 {:type :load, :mode :memloc_register, :reg1 :de, :reg2 :a}
   0x16 {:type :load, :mode :register_d8, :reg1 :d}
   0x1A {:type :load, :mode :register_memloc, :reg1 :a, :reg2 :de}
   0x1E {:type :load, :mode :register_d8, :reg1 :e}

   0x21 {:type :load, :mode :register_d16, :reg1 :hl}
   0x22 {:type :load, :mode :memloc+_register, :reg1 :hl, :reg2 :a}
   0x26 {:type :load, :mode :register_d8, :reg1 :h}
   0x2A {:type :load, :mode :register_memloc+, :reg1 :a, :reg2 :hl}
   0x2E {:type :load, :mode :register_d8, :reg1 :l}

   0x31 {:type :load, :mode :register_d16, :reg1 :sp}
   0x32 {:type :load, :mode :memloc-_register, :reg1 :hl, :reg2 :a}
   0x36 {:type :load, :mode :register_d8, :reg1 :hl}
   0x3A {:type :load, :mode :register_memloc-, :reg1 :a, :reg2 :hl}
   0x3E {:type :load, :mode :register_d8, :reg1 :a}


   0x40 {:type :load, :mode :register_register, :reg1 :b, :reg2 :b}
   0x41 {:type :load, :mode :register_register, :reg1 :b, :reg2 :c}
   0x42 {:type :load, :mode :register_register, :reg1 :b, :reg2 :d}
   0x43 {:type :load, :mode :register_register, :reg1 :b, :reg2 :e}
   0x44 {:type :load, :mode :register_register, :reg1 :b, :reg2 :h}
   0x45 {:type :load, :mode :register_register, :reg1 :b, :reg2 :l}
   0x46 {:type :load, :mode :register_memloc, :reg1 :b, :reg2 :hl}
   0x47 {:type :load, :mode :register_register, :reg1 :b, :reg2 :a}
   0x48 {:type :load, :mode :register_register, :reg1 :c, :reg2 :b}
   0x49 {:type :load, :mode :register_register, :reg1 :c, :reg2 :c}
   0x4A {:type :load, :mode :register_register, :reg1 :c, :reg2 :d}
   0x4B {:type :load, :mode :register_register, :reg1 :c, :reg2 :e}
   0x4C {:type :load, :mode :register_register, :reg1 :c, :reg2 :h}
   0x4D {:type :load, :mode :register_register, :reg1 :c, :reg2 :l}
   0x4E {:type :load, :mode :register_memloc, :reg1 :c, :reg2 :hl}
   0x4F {:type :load, :mode :register_register, :reg1 :c, :reg2 :a}

   0x50 {:type :load, :mode :register_register, :reg1 :d, :reg2 :b}
   0x51 {:type :load, :mode :register_register, :reg1 :d, :reg2 :c}
   0x52 {:type :load, :mode :register_register, :reg1 :d, :reg2 :d}
   0x53 {:type :load, :mode :register_register, :reg1 :d, :reg2 :e}
   0x54 {:type :load, :mode :register_register, :reg1 :d, :reg2 :h}
   0x55 {:type :load, :mode :register_register, :reg1 :d, :reg2 :l}
   0x56 {:type :load, :mode :register_memloc, :reg1 :d, :reg2 :hl}
   0x57 {:type :load, :mode :register_register, :reg1 :d, :reg2 :a}
   0x58 {:type :load, :mode :register_register, :reg1 :e, :reg2 :b}
   0x59 {:type :load, :mode :register_register, :reg1 :e, :reg2 :c}
   0x5A {:type :load, :mode :register_register, :reg1 :e, :reg2 :d}
   0x5B {:type :load, :mode :register_register, :reg1 :e, :reg2 :e}
   0x5C {:type :load, :mode :register_register, :reg1 :e, :reg2 :h}
   0x5D {:type :load, :mode :register_register, :reg1 :e, :reg2 :l}
   0x5E {:type :load, :mode :register_memloc, :reg1 :e, :reg2 :hl}
   0x5F {:type :load, :mode :register_register, :reg1 :e, :reg2 :a}

   0x60 {:type :load, :mode :register_register, :reg1 :h, :reg2 :b}
   0x61 {:type :load, :mode :register_register, :reg1 :h, :reg2 :c}
   0x62 {:type :load, :mode :register_register, :reg1 :h, :reg2 :d}
   0x63 {:type :load, :mode :register_register, :reg1 :h, :reg2 :e}
   0x64 {:type :load, :mode :register_register, :reg1 :h, :reg2 :h}
   0x65 {:type :load, :mode :register_register, :reg1 :h, :reg2 :l}
   0x66 {:type :load, :mode :register_memloc, :reg1 :h, :reg2 :hl}
   0x67 {:type :load, :mode :register_register, :reg1 :h, :reg2 :a}
   0x68 {:type :load, :mode :register_register, :reg1 :l, :reg2 :b}
   0x69 {:type :load, :mode :register_register, :reg1 :l, :reg2 :c}
   0x6A {:type :load, :mode :register_register, :reg1 :l, :reg2 :d}
   0x6B {:type :load, :mode :register_register, :reg1 :l, :reg2 :e}
   0x6C {:type :load, :mode :register_register, :reg1 :l, :reg2 :h}
   0x6D {:type :load, :mode :register_register, :reg1 :l, :reg2 :l}
   0x6E {:type :load, :mode :register_memloc, :reg1 :l, :reg2 :hl}
   0x6F {:type :load, :mode :register_register, :reg1 :l, :reg2 :a}

   0x70 {:type :load, :mode :memloc_register, :reg1 :hl, :reg2 :b}
   0x71 {:type :load, :mode :memloc_register, :reg1 :hl :reg2 :c}
   0x72 {:type :load, :mode :memloc_register, :reg1 :hl :reg2 :d}
   0x73 {:type :load, :mode :memloc_register, :reg1 :hl :reg2 :e}
   0x74 {:type :load, :mode :memloc_register, :reg1 :hl :reg2 :h}
   0x75 {:type :load, :mode :memloc_register, :reg1 :hl :reg2 :l}
   0x76 {:type :halt}
   0x77 {:type :load, :mode :memloc_register, :reg1 :hl :reg2 :a}
   0x78 {:type :load, :mode :register_register, :reg1 :a, :reg2 :b}
   0x79 {:type :load, :mode :register_register, :reg1 :a, :reg2 :c}
   0x7A {:type :load, :mode :register_register, :reg1 :a, :reg2 :d}
   0x7B {:type :load, :mode :register_register, :reg1 :a, :reg2 :e}
   0x7C {:type :load, :mode :register_register, :reg1 :a, :reg2 :h}
   0x7D {:type :load, :mode :register_register, :reg1 :a, :reg2 :l}
   0x7E {:type :load, :mode :register_memloc, :reg1 :a, :reg2 :hl}
   0x7F {:type :load, :mode :register_register, :reg1 :a, :reg2 :a}

   0xAF {:type :xor, :mode :register, :reg1 :a}
   0xC3 {:type :jump, :mode :d16}

   0xE0 {:type :loadh, :mode :a8_register, :reg2 :a}
   0xE2 {:type :load, :mode :memloc_register, :reg1 :c, :reg2 :a}
   0xEA {:type :load, :mode :d16_register, :reg2 :a}

   0xF0 {:type :loadh, :mode :register_a8, :reg1 :a}
   0xF2 {:type :load, :mode :register_memloc, :reg1 :a, :reg2 :c}
   0xF3 {:type :di, :mode :implied}
   0xF8 {:type :load, :mode :register_sp-plus-r8, :reg1 :hl}
   0xFA {:type :load, :mode :register_a16, :reg1 :a}})

(defn for-opcode [opcode]
  (or (instructions (b/to-unsigned opcode))
      (throw (Exception. (format "Unknown opcode %02X" opcode)))))

(defn init []
 {:type nil, :mode nil, :reg1 nil, :reg2 nil, :cond nil, :param 0})

(comment

  nil)
