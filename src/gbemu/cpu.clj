(ns gbemu.cpu
  (:require [gbemu.instruction :as i]
            [gbemu.bus :as bus]
            [gbemu.execution :as exec]))


(defn init []
  {:registers {:a 0, :f 0,
               :b 0, :c 0,
               :d 0, :e 0,
               :h 0, :l 0,
               :pc 0x100, :sp 0}
   :fetched-data 0,
   :mem_dest 0,
   :dest_is_mem false,
   :cur_opcode 0,
   :cur-instr nil
   :halted false
   :stepping false
   :int_master_enabled false})

(defn read-reg [ctx r]
  (let [regs (get-in ctx [:cpu :registers])]
    (case r
      :af (bit-or (bit-shift-left (regs :a) 8) (regs :f))
      :bc (bit-or (bit-shift-left (regs :b) 8) (regs :c))
      :de (bit-or (bit-shift-left (regs :d) 8) (regs :e))
      :hl (bit-or (bit-shift-left (regs :h) 8) (regs :l))
      (regs r))))

(defn fetch-instruction [ctx]
  (let [pc (get-in ctx [:cpu :registers :pc])
        op (bus/read-bus ctx pc)
        _ (println "fetching inst " pc op)]
    (or op (throw (Exception. "Unknown opcode")))
    (-> ctx
        (update :cpu assoc :cur_opcode op
                           :cur-instr (i/for-opcode op))
        (assoc-in [:cpu :registers :pc] (inc pc)))))

(defn fetch-data [ctx]
  (let [inst (:cur-instr (:cpu ctx))
        regs (:registers (:cpu ctx))
        pc (:pc regs)]
    (update ctx :cpu merge
      {:mem_dest 0 :dest_is_mem false}
      (case (inst :mode)
        :implied {}
        :register {:fetched-data (read-reg (:reg_1 inst))}
        :d8_to_register {:emu-cycles 1
                         :pc (inc pc)
                         :fetched-data (bus/read-bus ctx pc)}
        :d16 (let [lo (bus/read-bus ctx pc)
                   hi (bus/read-bus ctx (inc pc))]
               {:emu-cycles 2
                ;; TODO unchecked byte to truncate
                :fetched-data (bit-or lo (bit-shift-left hi 8))
                :pc (+ pc 2)})))))

(defn step [ctx]
  (println (str "step " ctx))
  (if (not (:halted (:cpu ctx)))
     (let [
           ;; _ (println (str "ctx before fetch-instr" ctx))
           ctx' (fetch-instruction ctx)
           ;; _ (println (str "ctx after fetch-instr" ctx'))
           ctx' (fetch-data ctx')
           ;; _ (println (str "ctx after fetch-data" ctx'))
           ctx' (exec/execute ctx')]
       ctx')))


(comment
  (println "sup")
  (println 2r101))
