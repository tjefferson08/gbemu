(ns gbemu.execution.load
  (:require [gbemu.cpu.registers :as r]
            [gbemu.bus :as bus]
            [gbemu.execution.flags :as flags]
            [gbemu.bytes :as bytes]))

(defn load [ctx]
  (let [{:keys [fetched-data cur-instr mem_dest dest_is_mem] :as cpu} (:cpu ctx)
        {:keys [mode reg1 reg2] :as inst} cur-instr
        _ (println cpu)]
    (cond
      (and dest_is_mem reg2 (r/sixteen-bit? reg2)) (bus/write-bus-16 ctx mem_dest fetched-data)
      dest_is_mem                                  (bus/write-bus ctx mem_dest fetched-data)
      (= mode :register_sp-plus-r8)                (let [sp    (r/read-reg ctx :sp)
                                                         hflag (< 0x0F (bytes/mask-sum 0x0F fetched-data sp))
                                                         cflag (< 0xFF (bytes/mask-sum 0xFF fetched-data sp))
                                                         sum   (+ sp (bytes/extend-sign fetched-data))]
                                                     (-> ctx
                                                         (flags/set-flags {:z false, :n false, :h hflag :c cflag})
                                                         (r/write-reg reg1 sum)))

      :else       (r/write-reg ctx reg1 fetched-data))))

(defn load-high-ram [ctx]
  (let [{:keys [cur-instr fetched-data mem_dest]} (:cpu ctx)
        {:keys [reg1 reg2]}              cur-instr
        ;; _ (println "load-h ctx" (:cpu ctx))
        ctx'                             (if (= reg1 :a)
                                           (r/write-reg ctx reg1 (bus/read-bus ctx (bit-or 0xFF00 fetched-data)))
                                           (bus/write-bus ctx mem_dest (r/read-reg ctx reg2)))]
        ;; _ (println "load-h ctx'" (:cpu ctx'))]
    (assoc-in ctx' [:cpu :emu-cycles] 1)))

(comment
  (format "%04X" (- 0xFF00 0xC000))

 ,)
