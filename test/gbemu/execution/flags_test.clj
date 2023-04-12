(ns gbemu.execution.flags-test
  (:require [gbemu.execution.flags :as sut]
            [clojure.test :refer :all]
            [gbemu.cpu.registers :as r]))

(def default-ctx {:cpu {:cur-instr nil, :fetched-data 0xA6 :registers {:pc 0x99 :a 0xAA :f 0}}})

(deftest set-flags
  (let [ctx-ff (assoc-in default-ctx [:cpu :registers :f] 0xFF)
        ctx-00 (assoc-in default-ctx [:cpu :eegisters :f] 0x00)]
   (is (= 2r01111111 (-> (sut/set-flags ctx-ff {:z false}) (r/read-reg :f))))
   (is (= 2r10111111 (-> (sut/set-flags ctx-ff {:n false}) (r/read-reg :f))))
   (is (= 2r11011111 (-> (sut/set-flags ctx-ff {:h false}) (r/read-reg :f))))
   (is (= 2r11101111 (-> (sut/set-flags ctx-ff {:c false}) (r/read-reg :f))))

   (is (= 2r10101111 (-> (sut/set-flags ctx-ff {:z true :n false :h true :c false}) (r/read-reg :f))))
   (is (= 2r01011111 (-> (sut/set-flags ctx-ff {:z false :n true :h false :c true}) (r/read-reg :f))))

   (is (= 2r10000000 (-> (sut/set-flags ctx-00 {:z true}) (r/read-reg :f))))
   (is (= 2r01000000 (-> (sut/set-flags ctx-00 {:n true}) (r/read-reg :f))))
   (is (= 2r00100000 (-> (sut/set-flags ctx-00 {:h true}) (r/read-reg :f))))
   (is (= 2r00010000 (-> (sut/set-flags ctx-00 {:c true}) (r/read-reg :f))))

   (is (= 2r10100000 (-> (sut/set-flags ctx-00 {:z true :n false :h true :c false}) (r/read-reg :f))))
   (is (= 2r01010000 (-> (sut/set-flags ctx-00 {:z false :n true :h false :c true}) (r/read-reg :f))))))
