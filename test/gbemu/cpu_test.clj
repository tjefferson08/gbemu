(ns gbemu.cpu-test
  (:require [gbemu.cpu :as sut]
            [clojure.test :refer :all]))

(deftest read-reg
  (let [ctx {:cpu {:registers {:a 0x12 :f 0xAA}}}]
    (is (= (sut/read-reg ctx :a) 0x12))
    (is (= (sut/read-reg ctx :f) 0xAA))
    (is (= (sut/read-reg ctx :af) 0x12AA))))
