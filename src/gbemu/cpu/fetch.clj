(ns gbemu.cpu.fetch
  (:require [gbemu.bus :as bus]
            [gbemu.cpu.registers :as r]))

(defn fetch-data [ctx]
  (let [inst (:cur-instr (:cpu ctx))
        regs (:registers (:cpu ctx))
        pc (:pc regs)]
    (update ctx :cpu merge
      {:mem_dest 0 :dest_is_mem false}
      (case (inst :mode)
        :implied {}
        :register {:fetched-data (r/read-reg ctx (:reg_1 inst))}
        :d8_to_register {:emu-cycles 1
                         :registers (assoc regs :pc (inc pc))
                         :fetched-data (bus/read-bus ctx pc)}
        :d16 (let [lo (bus/read-bus ctx pc)
                   hi (bus/read-bus ctx (inc pc))]
               {:emu-cycles 2
                ;; TODO unchecked byte to truncate
                :fetched-data (bit-or lo (bit-shift-left hi 8))
                :registers (assoc regs :pc (+ 2 pc))})))))
