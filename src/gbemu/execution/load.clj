(ns gbemu.execution.load
  (:require [gbemu.cpu.registers :as r]
            [gbemu.bus :as bus]
            [gbemu.execution.flags :as flags]))

(defn load [ctx]
  (let [{:keys [mode fetched-data cur-instr mem_dest dest_is_mem] :as cpu} (:cpu ctx)
        {:keys [reg1 reg2] :as inst} cur-instr]
        ;; _ (println cpu)]
    (cond
      (and dest_is_mem (r/sixteen-bit? reg2)) (bus/write-bus-16 ctx mem_dest fetched-data)
      dest_is_mem                            (bus/write-bus-16 ctx mem_dest fetched-data)
      (= mode :register_sp-plus-r8)          (let [hflag (< 0x10 (+ (bit-and 0x0F fetched-data)
                                                                    (bit-and 0x0F (r/read-reg ctx reg2))))
                                                   cflag (< 0x100 (+ (bit-and 0xFF fetched-data)
                                                                     (bit-and 0xFF (r/read-reg ctx reg2))))]
                                               (-> ctx
                                                   (flags/set-flags {:h hflag :c cflag})
                                                   (r/write-reg reg1 (bit-and 0xFF (+ (r/read-reg ctx reg2) fetched-data)))))

      :else       (r/write-reg ctx reg1 fetched-data))))

(defn load-high-ram [ctx]
  (let [{:keys [cur-instr fetched-data]} (:cpu ctx)
        {:keys [reg1 reg2]}              cur-instr
        ;; _ (println ctx)
        ctx'                             (if (= reg1 :a)
                                           (r/write-reg ctx reg1 (bus/read-bus (bit-or 0xFF00 fetched-data)))
                                           (bus/write-bus ctx (bit-or 0xFF00 fetched-data) (r/read-reg ctx reg2)))]
    (assoc-in ctx' [:cpu :emu-cycles] 1)))
