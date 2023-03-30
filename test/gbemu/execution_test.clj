(ns gbemu.execution-test
  (:require [gbemu.execution :as sut]
            [gbemu.instruction :as i]
            [clojure.test :refer :all]))

(def default-ctx {:cpu {:cur-instr nil, :fetched-data 0xA6} :registers {:pc 0x99}})

(deftest jump-instr
  (let [ctx-in-z-set (-> default-ctx
                         (assoc-in [:cpu :cur-instr] (i/for-opcode 0xC3))
                         (assoc-in [:cpu :registers :f] 0x01))
        ctx-in-z-unset (assoc-in ctx-in-z-set [:cpu :registers :f] 0x00)]
    (is (sut/flag-set? ctx-in-z-set :z))
    (is (not (sut/flag-set? ctx-in-z-unset :z)))

    (is (= 0xA6 (get-in (sut/execute ctx-in-z-set) [:cpu :registers :pc])))
    (is (= 0xA6 (get-in (sut/execute ctx-in-z-unset) [:cpu :registers :pc])))))
