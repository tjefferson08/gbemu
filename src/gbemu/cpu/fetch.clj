(ns gbemu.cpu.fetch
  (:require [gbemu.bus :as bus]
            [gbemu.cpu.registers :as r]))

;; TODO refactor to multimethod
(defn fetch-data [ctx]
  (let [inst (:cur-instr (:cpu ctx))
        regs (:registers (:cpu ctx))
        pc (:pc regs)
        reg1 (:reg1 inst)
        reg1_val (r/read-reg ctx reg1)
        reg2 (:reg2 inst)
        reg2_val (r/read-reg ctx reg2)
        r-update (fn [reg v] (get-in (r/write-reg ctx reg v) [:cpu :registers]))]
        ;; _ (println inst regs)]
    (update ctx :cpu merge
      {:mem_dest 0 :dest_is_mem false}
      (case (inst :mode)
        :a8_register {:mem_dest (bit-or 0xFF00 (bus/read-bus ctx pc))
                      :dest_is_mem true
                      :emu-cycles 1
                      :registers (r-update :pc (inc pc))}
        :implied {}
        :memloc_d8 {:fetched-data (bus/read-bus ctx pc)
                    :mem_dest reg1_val
                    :registers (r-update :pc (inc pc))
                    :dest_is_mem  true}
        :memloc_register {:fetched-data reg2_val
                          :mem_dest (if (= reg1 :c) (bit-or 0xFF reg1_val) reg1_val)
                          :dest_is_mem  true}
        :register {:fetched-data reg1_val}
        :register_register {:fetched-data reg2_val}
        :register_memloc (let [addr (if (= reg1 :c) (bit-or 0xFF reg2_val) reg2_val)]
                           {:fetched-data (bus/read-bus ctx addr)
                            :emu-cycles 1})
        (:d8 :register_a8 :register_d8) {:emu-cycles 1
                                         :registers (r-update :pc (inc pc))
                                         :fetched-data (bus/read-bus ctx pc)}
        :register_memloc+ {:fetched-data (bus/read-bus ctx reg2_val)
                           :emu-cycles 1
                           :registers (r-update reg2 (inc reg2_val))}
        :register_memloc- {:fetched-data (bus/read-bus ctx reg2_val)
                           :emu-cycles 1
                           :registers (r-update reg2 (dec reg2_val))}
        :register_sp-plus-r8 {:fetched-data (bus/read-bus ctx pc)
                              :emu-cycles 1
                              :registers (r-update :pc (inc pc))}
        :memloc+_register {:fetched-data (bus/read-bus ctx reg2_val)
                           :mem_dest reg1_val
                           :dest_is_mem true
                           :registers (r-update reg1 (inc reg1_val))}
        :memloc-_register {:fetched-data (bus/read-bus ctx reg2_val)
                           :mem_dest reg1_val
                           :dest_is_mem true
                           :registers (r-update reg1 (dec reg1_val))}

        :d16_register (let [lo  (bus/read-bus ctx pc)
                            hi  (bus/read-bus ctx (inc pc))
                            d16 (bit-or (bit-and 0x00FF lo) (bit-and 0xFF00 (bit-shift-left hi 8)))]
                        {:emu-cycles 2
                         ;; TODO unchecked byte to truncate
                         :fetched-data reg2_val
                         :mem_dest d16
                         :dest_is_mem true
                         :registers (r-update :pc (+ 2 pc))})

        (:d16 :register_d16) (let [lo  (bus/read-bus ctx pc)
                                   hi  (bus/read-bus ctx (inc pc))
                                   d16 (bit-or (bit-and 0x00FF lo) (bit-and 0xFF00 (bit-shift-left hi 8)))]
                               {:emu-cycles 2
                                ;; TODO unchecked byte to truncate
                                :fetched-data d16
                                :registers (r-update :pc (+ 2 pc))})))))


(comment

 (format "%04X" (bit-or -1 (bit-shift-left (bit-and 0x00ff -33) 8)))


 (unchecked-byte -1)

 (bit-shift-left 0xDF 8)
 (bit-or (bit-and 0x00FF -1) (bit-and 0xFF00 (bit-shift-left -33 8)))

 (bit-shift-left -33 8)

 (bit-or 0xFF 0xDF00)

 0xDFFF


 nil)
