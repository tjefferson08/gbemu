(ns gbemu.cpu
  (:require [gbemu.instruction :as i]))

(defn init []
  {:registers {:a 0, :f 0,
               :b 0, :c 0,
               :d 0, :e 0,
               :h 0, :l 0,
               :pc 0x100, :sp 0}
   :fetched_data 0,
   :mem_dest 0,
   :dest_is_mem false,
   :cur_opcode 0,
   :cur_inst nil
   :halted false
   :stepping false
   :int_master_enabled})


(defn step [ctx]
  (if (not (:halted ctx))
     (let [ctx' (fetch-instruction ctx)
           ctx'' (fetch-data ctx')
           (execute)])))

(defn fetch-instruction [ctx]
  (let [pc (get-in ctx [:registers :pc])
        op (bus/read-bus pc)]
    (or op (throw (Exception. "Unknown opcode")))
    (assoc ctx :pc (inc pc)
               :cur_opcode op
               :cur_instr (i/for-opcode op))))

(defn fetch-data [ctx]
  (let [inst (:cur_instr ctx)
        regs (:registers ctx)
        pc (:pc regs)]
    (merge ctx
      {:mem_dest 0 :dest_is_mem false}
      (case (inst :mode)
        :implied {}
        :register {:fetched_data (cpu/read-register (:reg_1 inst))}
        :d8_to_register {:emu-cycles 1
                         :pc (inc pc)
                         :fetched_data (bus/read-bus pc)}
        :d16 (let [lo (bus/read-bus pc)
                   hi (bus/read-bus (inc pc))]
               {:emu-cycles 2
                ;; TODO unchecked byte to truncate
                :fetched_data (bit-or lo (bit-shift-left hi 8))
                :pc (+ pc 2)})))))


(defn read-reg [ctx r]
  (let [regs (:registers ctx)]
    (case r
      :af (bit-or (regs :a) (bit-shift-left (regs :f) 8))
      :bc (bit-or (regs :b) (bit-shift-left (regs :c) 8))
      :de (bit-or (regs :d) (bit-shift-left (regs :e) 8))
      :hl (bit-or (regs :h) (bit-shift-left (regs :l) 8))
      (regs r))))

(defn execute [])

(comment
  (println "sup")
  (println 2r101))
