(ns gbemu.instruction)

(def instructions
  {0x00 {:type :no-op, :mode :implied}
   0x05 {:type :decrement, :mode :register, :reg_1 :b}
   0x0E {:type :load, :mode :d8_to_register, :reg_1 :c}
   0xAF {:type :xor, :mode :register, :reg_1 :a}
   0xC3 {:type :jump, :mode :d16}})

(defn for-opcode [opcode]
  (instructions opcode))

(defn init []
 {:type nil, :mode nil, :reg_1 nil, :reg_2 nil, :cond nil, :param 0})
