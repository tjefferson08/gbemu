(ns gbemu.cpu.core
  (:require [gbemu.instruction :as i]
            [gbemu.bus :as bus]
            [gbemu.execution :as exec]
            [gbemu.cpu.fetch :as fetch]))

(defn init []
  {:registers {:a 0x01, :f 0,
               :b 0, :c 0,
               :d 0, :e 0,
               :h 0, :l 0,
               :pc 0x100, :sp 0}
   :fetched-data 0,
   :mem_dest 0,
   :dest_is_mem false,
   :cur-opcode 0,
   :cur-instr nil
   :halted false
   :stepping false
   :int-master-enabled false})

(defn fetch-instruction [ctx]
  (let [pc (get-in ctx [:cpu :registers :pc])
        op (bus/read-bus ctx pc)]
        ;; _ (println "fetching inst " pc op)]
    (-> ctx
        (update :cpu assoc :cur-opcode op
                           :cur-instr (i/for-opcode op))
        (assoc-in [:cpu :registers :pc] (inc pc)))))

(defn step [ctx]
  ;; (println (str "step " ctx))
  (if (not (:halted (:cpu ctx)))
     (let [
           ;; _ (println (str "ctx before fetch-instr" ctx))
           ctx' (fetch-instruction ctx)
           ;; _ (println (str "ctx after fetch-instr" ctx'))
           ctx'' (fetch/fetch-data ctx')
           ;; _ (println (str "ctx after fetch-data" ctx''))
           pc   (get-in ctx [:cpu :registers :pc])
           _ (println (format "%04X: %-7s (%02X %02X %02X)"
                              pc
                              (get-in ctx'' [:cpu :cur-instr :type])
                              (get-in ctx'' [:cpu :cur-opcode])
                              (bus/read-bus ctx'' (+ pc 1))
                              (bus/read-bus ctx'' (+ pc 2))))
           ctx''' (exec/execute ctx'')]
       ctx''')))


(comment
  (println "sup")
  (format "%02X" 256)

  (merge-with merge {:cpu {:registers {:a 1 :b 2}}}
         {:cpu {:registers {:b 3}}})

  (println 2r101))
