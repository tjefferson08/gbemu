(ns gbemu.instruction
  (:require [gbemu.bytes :as b]))

(def instructions
  {0x00 {:type :no-op, :mode :implied}
   0x01 {:type :load, :mode :register_d16, :reg1 :bc}
   0x02 {:type :load, :mode :memloc_register, :reg1 :bc, :reg2 :a}
   0x03 {:type :increment, :mode :register :reg1 :bc}
   0x04 {:type :increment, :mode :register :reg1 :b}
   0x05 {:type :decrement, :mode :register, :reg1 :b}
   0x06 {:type :load, :mode :register_d8, :reg1 :b}
   0x08 {:type :load, :mode :a16_register, :reg2 :sp}
   0x09 {:type :add, :mode :register_register, :reg1 :hl, :reg2 :bc}
   0x0A {:type :load, :mode :register_memloc, :reg1 :a, :reg2 :bc}
   0x0B {:type :decrement, :mode :register, :reg1 :bc}
   0x0C {:type :increment, :mode :register :reg1 :c}
   0x0D {:type :decrement, :mode :register, :reg1 :c}
   0x0E {:type :load, :mode :register_d8, :reg1 :c}

   0x11 {:type :load, :mode :register_d16, :reg1 :de}
   0x12 {:type :load, :mode :memloc_register, :reg1 :de, :reg2 :a}
   0x13 {:type :increment, :mode :register :reg1 :de}
   0x14 {:type :increment, :mode :register :reg1 :d}
   0x15 {:type :decrement, :mode :register, :reg1 :d}
   0x16 {:type :load, :mode :register_d8, :reg1 :d}
   0x18 {:type :jump-rel, :mode :d8, :cond :always}
   0x19 {:type :add, :mode :register_register, :reg1 :hl, :reg2 :de}
   0x1A {:type :load, :mode :register_memloc, :reg1 :a, :reg2 :de}
   0x1B {:type :decrement, :mode :register, :reg1 :de}
   0x1C {:type :increment, :mode :register :reg1 :e}
   0x1D {:type :decrement, :mode :register, :reg1 :e}
   0x1E {:type :load, :mode :register_d8, :reg1 :e}

   0x20 {:type :jump-rel, :mode :d8, :cond :nz}
   0x21 {:type :load, :mode :register_d16, :reg1 :hl}
   0x22 {:type :load, :mode :memloc+_register, :reg1 :hl, :reg2 :a}
   0x23 {:type :increment, :mode :register :reg1 :hl}
   0x24 {:type :increment, :mode :register :reg1 :h}
   0x25 {:type :decrement, :mode :register, :reg1 :h}
   0x26 {:type :load, :mode :register_d8, :reg1 :h}
   0x28 {:type :jump-rel, :mode :d8, :cond :z}
   0x29 {:type :add, :mode :register_register, :reg1 :hl, :reg2 :hl}
   0x2A {:type :load, :mode :register_memloc+, :reg1 :a, :reg2 :hl}
   0x2B {:type :decrement, :mode :register, :reg1 :hl}
   0x2C {:type :increment, :mode :register :reg1 :l}
   0x2D {:type :decrement, :mode :register, :reg1 :l}
   0x2E {:type :load, :mode :register_d8, :reg1 :l}

   0x30 {:type :jump-rel, :mode :d8, :cond :nc}
   0x31 {:type :load, :mode :register_d16, :reg1 :sp}
   0x32 {:type :load, :mode :memloc-_register, :reg1 :hl, :reg2 :a}
   0x33 {:type :increment, :mode :register :reg1 :sp}
   0x34 {:type :increment, :mode :memloc :reg1 :hl}
   0x35 {:type :decrement, :mode :memloc, :reg1 :hl}
   0x36 {:type :load, :mode :memloc_d8, :reg1 :hl}
   0x38 {:type :jump-rel, :mode :d8, :cond :c}
   0x39 {:type :add, :mode :register_register, :reg1 :hl, :reg2 :sp}
   0x3A {:type :load, :mode :register_memloc-, :reg1 :a, :reg2 :hl}
   0x3B {:type :decrement, :mode :register, :reg1 :sp}
   0x3C {:type :increment, :mode :register :reg1 :a}
   0x3D {:type :decrement, :mode :register, :reg1 :a}
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
   0x76 {:type :halt, :mode :implied}
   0x77 {:type :load, :mode :memloc_register, :reg1 :hl :reg2 :a}
   0x78 {:type :load, :mode :register_register, :reg1 :a, :reg2 :b}
   0x79 {:type :load, :mode :register_register, :reg1 :a, :reg2 :c}
   0x7A {:type :load, :mode :register_register, :reg1 :a, :reg2 :d}
   0x7B {:type :load, :mode :register_register, :reg1 :a, :reg2 :e}
   0x7C {:type :load, :mode :register_register, :reg1 :a, :reg2 :h}
   0x7D {:type :load, :mode :register_register, :reg1 :a, :reg2 :l}
   0x7E {:type :load, :mode :register_memloc, :reg1 :a, :reg2 :hl}
   0x7F {:type :load, :mode :register_register, :reg1 :a, :reg2 :a}

   0x80 {:type :add, :mode :register_register, :reg1 :a, :reg2 :b}
   0x81 {:type :add, :mode :register_register, :reg1 :a, :reg2 :c}
   0x82 {:type :add, :mode :register_register, :reg1 :a, :reg2 :d}
   0x83 {:type :add, :mode :register_register, :reg1 :a, :reg2 :e}
   0x84 {:type :add, :mode :register_register, :reg1 :a, :reg2 :h}
   0x85 {:type :add, :mode :register_register, :reg1 :a, :reg2 :l}
   0x86 {:type :add, :mode :register_memloc, :reg1 :a, :reg2 :hl}
   0x87 {:type :add, :mode :register_register, :reg1 :a, :reg2 :a}
   0x88 {:type :adc, :mode :register_register, :reg1 :a, :reg2 :b}
   0x89 {:type :adc, :mode :register_register, :reg1 :a, :reg2 :c}
   0x8A {:type :adc, :mode :register_register, :reg1 :a, :reg2 :d}
   0x8B {:type :adc, :mode :register_register, :reg1 :a, :reg2 :e}
   0x8C {:type :adc, :mode :register_register, :reg1 :a, :reg2 :h}
   0x8D {:type :adc, :mode :register_register, :reg1 :a, :reg2 :l}
   0x8E {:type :adc, :mode :register_memloc, :reg1 :a, :reg2 :hl}
   0x8F {:type :adc, :mode :register_register, :reg1 :a, :reg2 :a}

   0x90 {:type :sub, :mode :register_register, :reg1 :a, :reg2 :b}
   0x91 {:type :sub, :mode :register_register, :reg1 :a, :reg2 :c}
   0x92 {:type :sub, :mode :register_register, :reg1 :a, :reg2 :d}
   0x93 {:type :sub, :mode :register_register, :reg1 :a, :reg2 :e}
   0x94 {:type :sub, :mode :register_register, :reg1 :a, :reg2 :h}
   0x95 {:type :sub, :mode :register_register, :reg1 :a, :reg2 :l}
   0x96 {:type :sub, :mode :register_memloc, :reg1 :a, :reg2 :hl}
   0x97 {:type :sub, :mode :register_register, :reg1 :a, :reg2 :a}
   0x98 {:type :sbc, :mode :register_register, :reg1 :a, :reg2 :b}
   0x99 {:type :sbc, :mode :register_register, :reg1 :a, :reg2 :c}
   0x9A {:type :sbc, :mode :register_register, :reg1 :a, :reg2 :d}
   0x9B {:type :sbc, :mode :register_register, :reg1 :a, :reg2 :e}
   0x9C {:type :sbc, :mode :register_register, :reg1 :a, :reg2 :h}
   0x9D {:type :sbc, :mode :register_register, :reg1 :a, :reg2 :l}
   0x9E {:type :sbc, :mode :register_memloc, :reg1 :a, :reg2 :hl}
   0x9F {:type :sbc, :mode :register_register, :reg1 :a, :reg2 :a}

   0xAF {:type :xor, :mode :register, :reg1 :a}

   0xC0 {:type :ret, :mode :implied, :cond :nz}
   0xC1 {:type :pop, :mode :implied, :reg1 :bc}
   0xC2 {:type :jump, :mode :d16, :cond :nz}
   0xC3 {:type :jump, :mode :d16, :cond :always}
   0xC4 {:type :call, :mode :d16, :cond :nz}
   0xC5 {:type :push, :mode :implied, :reg1 :bc}
   0xC7 {:type :rst, :mode :implied, :param 0x00}
   0xC8 {:type :ret, :mode :implied, :cond :z}
   0xC9 {:type :ret, :mode :implied, :cond :always}
   0xCA {:type :jump, :mode :d16, :cond :z}
   0xCC {:type :call, :mode :d16, :cond :z}
   0xCD {:type :call, :mode :d16, :cond :always}
   0xCE {:type :adc, :mode :register_d8, :reg1 :a}
   0xCF {:type :rst, :mode :implied, :param 0x08}

   0xD0 {:type :ret, :mode :implied, :cond :nc}
   0xD1 {:type :pop, :mode :implied, :reg1 :de}
   0xD2 {:type :jump, :mode :d16, :cond :nc}
   0xD4 {:type :call, :mode :d16, :cond :nc}
   0xD5 {:type :push, :mode :implied, :reg1 :de}
   0xD6 {:type :sub, :mode :register_d8, :reg1 :a}
   0xD7 {:type :rst, :mode :implied, :param 0x10}
   0xD8 {:type :ret, :mode :implied, :cond :c}
   0xD9 {:type :ret-i, :mode :implied, :cond :always}
   0xDA {:type :jump, :mode :d16, :cond :c}
   0xDC {:type :call, :mode :d16, :cond :c}
   0xDF {:type :rst, :mode :implied, :param 0x18}

   0xE0 {:type :loadh, :mode :a8_register, :reg2 :a}
   0xE1 {:type :pop, :mode :implied, :reg1 :hl}
   0xE2 {:type :load, :mode :memloc_register, :reg1 :c, :reg2 :a}
   0xE5 {:type :push, :mode :implied, :reg1 :hl}
   0xE7 {:type :rst, :mode :implied, :param 0x20}
   0xE8 {:type :add, :mode :register_d8, :reg1 :sp}
   0xE9 {:type :jump, :mode :memloc}
   0xEA {:type :load, :mode :d16_register, :reg2 :a}
   0xEF {:type :rst, :mode :implied, :param 0x28}

   0xF0 {:type :loadh, :mode :register_a8, :reg1 :a}
   0xF1 {:type :pop, :mode :implied, :reg1 :af}
   0xF2 {:type :load, :mode :register_memloc, :reg1 :a, :reg2 :c}
   0xF3 {:type :di, :mode :implied}
   0xF5 {:type :push, :mode :implied, :reg1 :af}
   0xF7 {:type :rst, :mode :implied, :param 0x30}
   0xF8 {:type :load, :mode :register_sp-plus-r8, :reg1 :hl}
   0xFA {:type :load, :mode :register_a16, :reg1 :a}
   0xFF {:type :rst, :mode :implied, :param 0x38}})

(defn for-opcode [opcode]
  (or (instructions (b/to-unsigned opcode))
      (throw (Exception. (format "Unknown opcode %02X" opcode)))))

(defn init []
 {:type nil, :mode nil, :reg1 nil, :reg2 nil, :cond nil, :param 0})

(comment

  nil)
