(ns gbemu.execution.core
  (:require [gbemu.cpu.registers :as r]
            [gbemu.bus :as bus]
            [clojure.pprint :refer [pprint]]
            [gbemu.stack :as stack]
            [gbemu.execution.load :as load]
            [gbemu.execution.dec :as dec]
            [gbemu.execution.jump :as jump]
            [gbemu.execution.xor :as xor]
            [gbemu.execution.math :as math]))

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
        ;; _ (println "ctx after push" (:cpu ctx'))]
    ctx'))

(defn- halt [ctx]
  (assoc-in ctx [:cpu :halted] true))

(def by-instruction
  {:none none
   :no-op identity
   :jump jump/jump
   :call jump/call
   :ret jump/ret
   :ret-i jump/ret-i
   :jump-rel jump/jump-rel
   :rst jump/rst
   :di di
   :add math/add
   :xor xor/xor
   :decrement math/decrement
   :increment math/increment
   :load load/load
   :pop pop
   :push push
   :halt halt
   :loadh load/load-high-ram})

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
