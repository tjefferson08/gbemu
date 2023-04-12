(ns gbemu.execution.core-test
  (:require [gbemu.execution.core :as sut]
            [gbemu.instruction :as i]
            [clojure.test :refer :all]
            [gbemu.cpu.core :as cpu]
            [gbemu.cpu.registers :as reg]
            [gbemu.execution.flags :as flags]))

(def default-ctx {:cpu {:cur-instr nil, :fetched-data 0xA6 :registers {:pc 0x99 :a 0xAA :f 0}}})

(deftest jump-instr
  (let [ctx-in-z-set (-> default-ctx
                         (assoc-in [:cpu :cur-instr] (i/for-opcode 0xC3))
                         (flags/set-flags {:z true}))
        ctx-in-z-unset (flags/set-flags ctx-in-z-set {:z false})]
    (is (flags/flag-set? ctx-in-z-set :z))

    (is (not (flags/flag-set? ctx-in-z-unset :z)))

    (is (= 0xA6 (get-in (sut/execute ctx-in-z-set) [:cpu :registers :pc])))
    (is (= 0xA6 (get-in (sut/execute ctx-in-z-unset) [:cpu :registers :pc])))))

(deftest xor-instr
  (let [ctx-in-normal-xor (-> default-ctx (assoc-in [:cpu :cur-instr] (i/for-opcode 0xAF)))
        ctx-out-normal-xor (sut/execute ctx-in-normal-xor)]
    (is (= 0x0C (get-in ctx-out-normal-xor [:cpu :registers :a])))
    (is (not (flags/flag-set? ctx-out-normal-xor :z))))

  (let [ctx-in-zero-result-xor (-> default-ctx (assoc-in [:cpu :cur-instr] (i/for-opcode 0xAF))
                                               (assoc-in [:cpu :registers :a] 0x6A)
                                               (assoc-in [:cpu :fetched-data] 0x6A))
        ctx-out-zero-result-xor (sut/execute ctx-in-zero-result-xor)]

    (is (= 0x00 (get-in ctx-out-zero-result-xor [:cpu :registers :a])))
    (is (flags/flag-set? ctx-out-zero-result-xor :z))))

(comment
  (format "%02X" (bit-xor 0xAA 0xAA))



 nil)
