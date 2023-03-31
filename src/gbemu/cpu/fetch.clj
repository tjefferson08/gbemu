(ns gbemu.cpu.fetch
  (:require [gbemu.bus :as bus]
            [gbemu.cpu.registers :as r]))

(defn fetch-data [ctx]
  (let [inst (:cur-instr (:cpu ctx))
        regs (:registers (:cpu ctx))
        pc (:pc regs)
        reg_1 (:reg1 inst)
        reg_1_val (r/read-reg reg_1)
        reg_2 (:reg2 inst)
        reg_2_val (r/read-reg reg_2)]
    (update ctx :cpu merge
      {:mem_dest 0 :dest_is_mem false}
      (case (inst :mode)
        :implied {}
        :register {:fetched-data reg_1_val}
        :register_to_register {:fetched-data reg_2_val}
        :memory_to_register {:fetched-data reg_2_val
                             :mem_dest (if (= reg_1 :c) (bit-or 0xFF reg_2_val) reg_2_val)
                             :dest_is_mem  true}
        :register_to_memory (let [addr (if (= reg_1 :c) (bit-or 0xFF reg_2_val) reg_2_val)]
                              {:fetched-data (bus/read-bus ctx addr)
                               :emu-cycles 1})
        :d8_to_register {:emu-cycles 1
                         :registers (assoc regs :pc (inc pc))
                         :fetched-data (bus/read-bus ctx pc)}
        ;; :register_to_hl+ {:fetched-data reg_2_value
        ;;                   :emu-cycles 1
        ;;                   :registers (update regs :hl inc)}
        ;; :register_to_hl- {:fetched-data reg_2_value
        ;;                   :emu-cycles 1
        ;;                   :registers (update regs :hl dec)}
        ;; :hl+_to_register {}
        ;; :hl-_to_register {}
        (:register_d16 :d16) (let [lo (bus/read-bus ctx pc)
                                   hi (bus/read-bus ctx (inc pc))]
                               {:emu-cycles 2
                                ;; TODO unchecked byte to truncate
                                :fetched-data (bit-or lo (bit-shift-left hi 8))
                                :registers (assoc regs :pc (+ 2 pc))})))))
