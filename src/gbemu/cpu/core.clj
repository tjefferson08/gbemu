(ns gbemu.cpu.core
  (:require [gbemu.instruction :as i]
            [gbemu.bus :as bus]
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

(def INTERRUPT_BITS {:vblank 0, :lcd-stat 1, :timer 2, :serial 3, :joypad 4})

(defn request-interrupt [ctx type]
  (let [bit (INTERRUPT_BITS type)]
    (update-in ctx [:cpu :int-flags] (fn [fl] (bit-set fl bit)))))

(defn fetch-instruction [ctx]
  (let [pc (r/read-reg ctx :pc)
        op (bus/read-bus ctx pc)]
        ;; _ (println "fetching inst " pc (i/for-opcode op))]
    (-> ctx
        (update :cpu assoc :cur-opcode op
                           :cur-instr (i/for-opcode op))
        (assoc-in [:cpu :registers :pc] (inc pc)))))

(defn- step-halted [ctx]
  (let [ctx' (clock/tick ctx 4)]
    (if (pos? (get-in ctx' [:cpu :int-flags]))
       (assoc-in ctx' [:cpu :halted] false)
       ctx')))

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
      (bus/read-bus ctx pc)
      (bus/read-bus ctx (+ 1 pc))
      (bus/read-bus ctx (+ 2 pc))
      (bus/read-bus ctx (+ 3 pc)))))

(defn- debug-log [ctx]
  (let [pc (r/read-reg ctx :pc)]
    (format "%04X - %8d: %80s (%02X %02X %02X) A:%02X F:%s%s%s%s BC:%02X%02X DE:%02X%02X HL:%02X%02X SP:%04X"
             pc
             (get-in ctx [:emu :ticks])
             (get-in ctx [:cpu :cur-instr])
             (get-in ctx [:cpu :cur-opcode])
             (bus/read-bus ctx (+ pc 1))
             (bus/read-bus ctx (+ pc 2))
             (r/read-reg ctx :a)
             (if (flags/flag-set? ctx :z) "Z" "-")
             (if (flags/flag-set? ctx :n) "N" "-")
             (if (flags/flag-set? ctx :h) "H" "-")
             (if (flags/flag-set? ctx :c) "C" "-")
             (r/read-reg ctx :b)
             (r/read-reg ctx :c)
             (r/read-reg ctx :d)
             (r/read-reg ctx :e)
             (r/read-reg ctx :h)
             (r/read-reg ctx :l)
             (r/read-reg ctx :sp))))

(defn- step-running [ctx]
  (let [
        ;; _ (println (str "ctx before fetch-instr" (:cpu ctx)))
        ctx' (clock/tick (fetch-instruction ctx) 4)

        ;; _ (println (str "ctx after fetch-instr" (:cpu ctx')))
        ctx'' (fetch/fetch-data ctx')
        ;; _ (println (str "ctx after fetch-data" (:cpu ctx'')))
        _ (.write (:log ctx'') (str (doctor-log ctx) "\n"))
        ;; _ (println (debug-log ctx))

        ctx''' (debug/update ctx'')
        _ (debug/print ctx''')
        ctx'''' (exec/execute ctx''')]
   ctx''''))

(defn- handle-interrupts [ctx]
  (let [handle (fn handle [ctx type]
                 (-> ctx
                   (stack/push-16 (r/read-reg ctx :pc))
                   (r/write-reg :pc (interrupt/routine-for type))
                   (interrupt/clear :type)
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
               (step-running ctx))]
    (cond
      (get-in ctx' [:cpu :enabling-ime])       (assoc-in ctx' [:cpu :int-master-enabled] true)
      (get-in ctx' [:cpu :int-master-enabled]) (-> ctx' (handle-interrupts)
                                                        (assoc-in [:cpu :enabling-ime] false))
      :else                                    ctx')))

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
