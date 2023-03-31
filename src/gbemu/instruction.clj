(ns gbemu.instruction
  (:require [gbemu.bytes :as b]))

(def instructions
  {0x00 {:type :no-op, :mode :implied}
   0x05 {:type :decrement, :mode :register, :reg_1 :b}
   0x0E {:type :load, :mode :d8_to_register, :reg_1 :c}
   0xAF {:type :xor, :mode :register, :reg_1 :a}
   0xC3 {:type :jump, :mode :d16}
   0xF3 {:type :di, :mode :implied}})

(defn for-opcode [opcode]
  (or (instructions (b/to-unsigned opcode))
      (throw (Exception. (format "Unknown opcode %02X" opcode)))))

(defn init []
 {:type nil, :mode nil, :reg_1 nil, :reg_2 nil, :cond nil, :param 0})

(comment

  nil)
