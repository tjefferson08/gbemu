(ns gbemu.execution.core
  (:require [gbemu.cpu.registers :as r]
            [gbemu.bus :as bus]
            [clojure.pprint :refer [pprint]]
            [gbemu.stack :as stack]
            [gbemu.execution.load :as load]
            [gbemu.execution.dec :as dec]
            [gbemu.execution.jump :as jump]
            [gbemu.execution.math :as math]
            [gbemu.execution.logic :as logic]
            [gbemu.execution.cb :as cb]
            [gbemu.execution.flags :as flags]
            [gbemu.bytes :as bytes]
            [gbemu.clock :as clock]))

(defn- none [ctx]
  (throw (Exception. "No instruction! Fail")))

(defn- di [ctx]
  (assoc-in ctx [:cpu :int-master-enabled] false))

(defn- pop [ctx]
  ;; TODO: emu-cycles b/t each 8bit stack pop
  (let [[val ctx'] (stack/pop-16 ctx)
        cur-instr (get-in ctx' [:cpu :cur-instr])
        reg1 (:reg1 cur-instr)]
    (if (= reg1 :af)
      (r/write-reg ctx' reg1 (bit-and 0xFFF0 val))
      (r/write-reg ctx' reg1 val))))

(defn- push [ctx]
  (let [cur-instr (get-in ctx [:cpu :cur-instr])
        reg1 (:reg1 cur-instr)
        ;; _ (println "ctx before push" (:cpu ctx))
        ctx' (stack/push-16 ctx (r/read-reg ctx reg1))]

    ;; extra tick for 16-bit register op
    (clock/tick ctx' 4)))

(defn- stop [ctx]
  (assoc-in ctx [:cpu :stopped] true))

(defn- halt [ctx]
  (assoc-in ctx [:cpu :halted] true))

(defn daa [ctx]
  (let [{:keys [c n h]} (flags/all ctx)
        a               (r/read-reg ctx :a)
        v               0
        v'              (if (or h (and (not n) (< 0x09 (bit-and 0x0F a))))
                          0x06
                          v)
        [v'' c']        (if (or c (and (not n) (< 0x99 a)))
                          [(bit-or 0x60 v') true]
                          [v' false])
        a'              (bytes/to-unsigned (if n (- a v'') (+ a v'')))]
     (-> ctx (r/write-reg :a a')
             (flags/set-flags {:z (zero? a'), :h false, :c c'}))))

(defn- cpl [ctx]
  (let [a (r/read-reg ctx :a)]
     (-> ctx (r/write-reg :a (bit-xor 0xFF a))
             (flags/set-flags {:n true, :h true}))))


(defn- scf [ctx]
   (flags/set-flags ctx {:n false, :h false, :c true}))

(defn- ccf [ctx]
  (let [c (flags/flag-set? ctx :c)]
    (flags/set-flags ctx {:n false, :h false, :c (not c)})))

(defn- ei [ctx]
  (assoc-in ctx [:cpu :enabling-ime] true))

(defn by-instruction [op]
  (op {:none none
       :no-op identity
       :jump jump/jump
       :call jump/call
       :ret jump/ret
       :ret-i jump/ret-i
       :jump-rel jump/jump-rel
       :rst jump/rst
       :di di
       :add math/add
       :adc math/adc
       :sub math/sub
       :sbc math/sbc
       :and logic/and
       :or logic/or
       :xor logic/xor
       :cp logic/cp
       :rlca logic/rlca
       :rrca logic/rrca
       :rla logic/rla
       :rra logic/rra
       :cb cb/cb-prefix
       :daa daa
       :cpl cpl
       :scf scf
       :ccf ccf
       :ei ei
       :decrement math/decrement
       :increment math/increment
       :load load/load
       :pop pop
       :push push
       :halt halt
       :stop stop
       :loadh load/load-high-ram}))

(defn execute [ctx]
  (let [inst (get-in ctx [:cpu :cur-instr])
        ;; _ (println inst)
        f (by-instruction (:type inst))]
        ;; _ (println f)]
    (if f
      (f ctx)
      (throw (Exception. (str "Unhandled instruction" inst))))))

(comment
  (format "%02X"
          (bit-and 0xFF (bit-set 0 7)))

  nil)
