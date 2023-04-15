(ns gbemu.cpu.registers-test
  (:require [gbemu.cpu.registers :as sut]
            [clojure.test :refer :all]))

(deftest read-reg
  (let [ctx {:cpu {:registers {:a 0x12 :f 0xAA}}}]
    (is (= (sut/read-reg ctx :a) 0x12))
    (is (= (sut/read-reg ctx :f) 0xAA))
    (is (= (sut/read-reg ctx :af) 0x12AA))))

(deftest write-reg
  (let [ctx        {:cpu {:registers {:a 0x12 :f 0xAA}}}
        updated-a  (sut/write-reg ctx :a 0x11)
        updated-af (sut/write-reg ctx :af 0x6116)]

    (is (= 0x11 (sut/read-reg updated-a :a)))
    (is (= 0x61 (sut/read-reg updated-af :a)))
    (is (= 0x16 (sut/read-reg updated-af :f)))))

(deftest eight-bit?
  (is (sut/eight-bit? :a)))

(deftest sixteen-bit?
  (is (sut/sixteen-bit? :sp)))
