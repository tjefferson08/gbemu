(ns gbemu.execution-test
  (:require [gbemu.execution :as sut]
            [gbemu.instruction :as i]
            [clojure.test :refer :all]
            [gbemu.cpu.core :as cpu]
            [gbemu.cpu.registers :as reg]))

(def default-ctx {:cpu {:cur-instr nil, :fetched-data 0xA6 :registers {:pc 0x99 :a 0xAA :f 0}}})

(deftest set-flags
  (let [ctx-ff (assoc-in default-ctx [:cpu :registers :f] 0xFF)
        ctx-00 (assoc-in default-ctx [:cpu :registers :f] 0x00)]
   (is (= 2r01111111 (-> (sut/set-flags ctx-ff {:z false}) (reg/read-reg :f))))
   (is (= 2r10111111 (-> (sut/set-flags ctx-ff {:n false}) (reg/read-reg :f))))
   (is (= 2r11011111 (-> (sut/set-flags ctx-ff {:h false}) (reg/read-reg :f))))
   (is (= 2r11101111 (-> (sut/set-flags ctx-ff {:c false}) (reg/read-reg :f))))

   (is (= 2r10101111 (-> (sut/set-flags ctx-ff {:z true :n false :h true :c false}) (reg/read-reg :f))))
   (is (= 2r01011111 (-> (sut/set-flags ctx-ff {:z false :n true :h false :c true}) (reg/read-reg :f))))

   (is (= 2r10000000 (-> (sut/set-flags ctx-00 {:z true}) (reg/read-reg :f))))
   (is (= 2r01000000 (-> (sut/set-flags ctx-00 {:n true}) (reg/read-reg :f))))
   (is (= 2r00100000 (-> (sut/set-flags ctx-00 {:h true}) (reg/read-reg :f))))
   (is (= 2r00010000 (-> (sut/set-flags ctx-00 {:c true}) (reg/read-reg :f))))

   (is (= 2r10100000 (-> (sut/set-flags ctx-00 {:z true :n false :h true :c false}) (reg/read-reg :f))))
   (is (= 2r01010000 (-> (sut/set-flags ctx-00 {:z false :n true :h false :c true}) (reg/read-reg :f))))))

(deftest jump-instr
  (let [ctx-in-z-set (-> default-ctx
                         (assoc-in [:cpu :cur-instr] (i/for-opcode 0xC3))
                         (sut/set-flags {:z true}))
        ctx-in-z-unset (sut/set-flags ctx-in-z-set {:z false})]
    (is (sut/flag-set? ctx-in-z-set :z))

    (is (not (sut/flag-set? ctx-in-z-unset :z)))

    (is (= 0xA6 (get-in (sut/execute ctx-in-z-set) [:cpu :registers :pc])))
    (is (= 0xA6 (get-in (sut/execute ctx-in-z-unset) [:cpu :registers :pc])))))

(deftest xor-instr
  (let [ctx-in-normal-xor (-> default-ctx (assoc-in [:cpu :cur-instr] (i/for-opcode 0xAF)))
        ctx-out-normal-xor (sut/execute ctx-in-normal-xor)]
    (is (= 0x0C (get-in ctx-out-normal-xor [:cpu :registers :a])))
    (is (not (sut/flag-set? ctx-out-normal-xor :z))))

  (let [ctx-in-zero-result-xor (-> default-ctx (assoc-in [:cpu :cur-instr] (i/for-opcode 0xAF))
                                               (assoc-in [:cpu :registers :a] 0x6A)
                                               (assoc-in [:cpu :fetched-data] 0x6A))
        ctx-out-zero-result-xor (sut/execute ctx-in-zero-result-xor)]

    (is (= 0x00 (get-in ctx-out-zero-result-xor [:cpu :registers :a])))
    (is (sut/flag-set? ctx-out-zero-result-xor :z))))

(comment
  (format "%02X" (bit-xor 0xAA 0xAA))



 nil)
