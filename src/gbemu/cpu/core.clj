(ns gbemu.cpu.core
  (:require [gbemu.instruction :as i]
            [gbemu.bus :as bus]
            [gbemu.execution.core :as exec]
            [gbemu.cpu.fetch :as fetch]
            [gbemu.cpu.registers :as r]
            [gbemu.execution.flags :as flags]))

(defn init []
  {:registers {:a 0x01, :f 0,
               :b 0, :c 0,
               :d 0, :e 0,
               :h 0, :l 0,
               :pc 0x100, :sp 0x0000}
   :fetched-data 0,
   :mem_dest 0,
   :dest_is_mem false,
   :cur-opcode 0,
   :cur-instr nil
   :halted false
   :stopped false
   :stepping false
   :int-master-enabled false
   :enabling-ime false
   :ie-register 0x00
   :int-flags 0x00})

(defn fetch-instruction [ctx]
  (let [pc (get-in ctx [:cpu :registers :pc])
        op (bus/read-bus ctx pc)]
        ;; _ (println "fetching inst " pc op)]
    (-> ctx
        (update :cpu assoc :cur-opcode op
                           :cur-instr (i/for-opcode op))
        (assoc-in [:cpu :registers :pc] (inc pc)))))

(defn- step-halted [ctx]
  ;; emu_cycles(1));
  (if (pos? (get-in ctx [:cpu :int-flags]))
     (assoc-in ctx [:cpu :halted] false)
     ctx))

(defn- step-running [ctx]
  (let [
        ;; _ (println (str "ctx before fetch-instr" (:cpu ctx)))
        ctx' (fetch-instruction ctx)
        ;; _ (println (str "ctx after fetch-instr" (:cpu ctx')))
        ctx'' (fetch/fetch-data ctx')
        ;; _ (println (str "ctx after fetch-data" (:cpu ctx'')))
        pc   (get-in ctx [:cpu :registers :pc])
        _ (println (format "%04X - %8d: %-12s (%02X %02X %02X) A:%02X F:%s%s%s%s BC:%02X%02X DE:%02X%02X HL:%02X%02X SP:%04X"
                            pc
                            (get-in ctx'' [:emu :ticks])
                            (get-in ctx'' [:cpu :cur-instr :type])
                            (get-in ctx'' [:cpu :cur-opcode])
                            (bus/read-bus ctx'' (+ pc 1))
                            (bus/read-bus ctx'' (+ pc 2))
                            (r/read-reg ctx'' :a)
                            (if (flags/flag-set? ctx'' :z) "Z" "-")
                            (if (flags/flag-set? ctx'' :n) "N" "-")
                            (if (flags/flag-set? ctx'' :h) "H" "-")
                            (if (flags/flag-set? ctx'' :c) "C" "-")
                            (r/read-reg ctx'' :b)
                            (r/read-reg ctx'' :c)
                            (r/read-reg ctx'' :d)
                            (r/read-reg ctx'' :e)
                            (r/read-reg ctx'' :h)
                            (r/read-reg ctx'' :l)
                            (r/read-reg ctx'' :sp)))
        ctx''' (exec/execute ctx'')]
   ctx'''))

(defn- handle-interrupts [ctx]
  ctx)

(defn step [ctx]
  (let [ctx' (if (get-in ctx [:cpu :halted])
               (step-halted ctx)
               (step-running ctx))]
    (cond
      (get-in ctx [:cpu :int-master-enabled]) (-> ctx' (handle-interrupts)
                                                       (assoc-in [:cpu :enabling-ime] false))
      (get-in ctx [:cpu :enabling-ime])       (assoc-in ctx' [:cpu :int-master-enabled] true)
      :else                                   ctx')))

(defn run [ctx]
  (loop [ctx' ctx]
    (if (get-in ctx' [:cpu :stopped])
      ctx'
      (recur (step ctx')))))

(comment
  (println "sup")
  (format "%02X" (bit-xor 1 1))
  (format "%s" "z")

  (merge-with merge {:cpu {:registers {:a 1 :b 2}}}
         {:cpu {:registers {:b 3}}})

  (println 2r101))
