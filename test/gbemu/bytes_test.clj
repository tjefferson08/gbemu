(ns gbemu.bytes-test
  (:require [gbemu.bytes :as sut]
            [clojure.test :refer :all]))

(deftest ->d16
  (is (= 0x3412 (sut/->d16 0x12 0x34)))
  (is (= 0xFFA0 (sut/->d16 0xA0 0xFF)))
  (is (= 0xA0FF (sut/->d16 0xFF 0xA0))))
