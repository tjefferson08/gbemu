(ns gbemu.cpu.core
  (:require [gbemu.instruction :as i]
            [gbemu.bus :as bus]
            [gbemu.bus.logical :as bus!]
            [gbemu.execution.core :as exec]
            [gbemu.cpu.fetch :as fetch]
            [gbemu.cpu.registers :as r]
            [gbemu.execution.flags :as flags]
            [gbemu.debug :as debug]
            [gbemu.log :as log]
            [gbemu.cpu.interrupt :as interrupt]
            [gbemu.stack :as stack]
            [gbemu.clock :as clock]))

(defn init []
  {:registers {:a 0x01, :f 0xB0,
               :b 0, :c 0x13,
               :d 0, :e 0xD8,
               :h 0x01, :l 0x4D
               :pc 0x100, :sp 0xFFFE}
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
   ;; TODO: timer goes to 0xABCC when cpu reset?
   :int-flags 0x00})

(defn fetch-instruction [ctx]
  (let [pc         (r/read-reg ctx :pc)
        [op, ctx'] (bus/read ctx pc)]
        ;; _ (println "fetching inst " pc (i/for-opcode op))]
    (-> ctx'
        (update :cpu assoc :cur-opcode op
                           :cur-instr (i/for-opcode op))
        (r/write-reg :pc (inc pc)))))

(defn- step-halted [ctx]
  (let [ctx' (clock/tick ctx 4)]
    (if (zero? (get-in ctx' [:cpu :int-flags]))
       ctx'
       (assoc-in ctx' [:cpu :halted] false))))

(defn- doctor-log [ctx]
  (let [pc (r/read-reg ctx :pc)]
    (format "A:%02X F:%02X B:%02X C:%02X D:%02X E:%02X H:%02X L:%02X SP:%04X PC:%04X PCMEM:%02X,%02X,%02X,%02X"
      (r/read-reg ctx :a)
      (r/read-reg ctx :f)
      (r/read-reg ctx :b)
      (r/read-reg ctx :c)
      (r/read-reg ctx :d)
      (r/read-reg ctx :e)
      (r/read-reg ctx :h)
      (r/read-reg ctx :l)
      (r/read-reg ctx :sp)
      pc
      (bus!/read! ctx pc)
      (bus!/read! ctx (+ 1 pc))
      (bus!/read! ctx (+ 2 pc))
      (bus!/read! ctx (+ 3 pc)))))

(defn- debug-log [ctx]
  (let [pc (r/read-reg ctx :pc)]
    (format "PC:%04X IF:%02X IE:%02X DIV:%04X TIMA:%02X TMA:%02X TAC:%02X"
             pc
             (r/read-interrupt-flags ctx)
             (r/read-ie-reg ctx)
             (get-in ctx [:timer :div])
             (get-in ctx [:timer :tima])
             (get-in ctx [:timer :tma])
             (get-in ctx [:timer :tac]))))

(defn- step-running [ctx]
  (let [ctx' (fetch-instruction ctx)
        ctx'' (fetch/fetch-data ctx')
        _ ((:log ctx'') (str (doctor-log ctx) "\n"))
        ctx''' (debug/update ctx'')
        _ (debug/print ctx''')
        ctx'''' (exec/execute ctx''')]
    ctx''''))

(defn- handle-interrupts [ctx]
  (let [handle (fn handle [ctx type]
                 (-> ctx
                   (stack/push-16 (r/read-reg ctx :pc))
                   (r/write-reg :pc (interrupt/routine-for type))
                   (interrupt/clear type)
                   (update :cpu assoc :halted false, :int-master-enabled false)))]
    (cond
      (interrupt/ready? ctx :vblank) (handle ctx :vblank)
      (interrupt/ready? ctx :lcd-stat) (handle ctx :lcd-stat)
      (interrupt/ready? ctx :timer) (handle ctx :timer)
      (interrupt/ready? ctx :serial) (handle ctx :serial)
      (interrupt/ready? ctx :joypad) (handle ctx :joypad)
      :else ctx)))

(defn step [ctx]
  (let [ctx' (if (get-in ctx [:cpu :halted])
               (step-halted ctx)
               (step-running ctx))
        ctx'' (if (get-in ctx' [:cpu :enabling-ime])
                (assoc-in ctx' [:cpu :int-master-enabled] true)
                ctx')
        ctx''' (if (get-in ctx'' [:cpu :int-master-enabled])
                 (-> ctx'' (handle-interrupts)
                           (assoc-in [:cpu :enabling-ime] false))
                 ctx'')]
    ctx'''))

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
