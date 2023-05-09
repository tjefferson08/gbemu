(ns gbemu.ui.core-test
  (:require [gbemu.ui.core :as sut]
            [clojure.test :refer :all]
            [gbemu.ppu :as ppu]
            [gbemu.bus :as bus]
            [clojure.set :as set]))

(deftest ppu-write
  (let [ctx (-> {:ppu (ppu/init)}
                (ppu/write-vram 0x8000 254))]))


(deftest all-tile-px
  ;; lifted example from Pandocs: https://gbdev.io/pandocs/Tile_Data.html
  (let [ctx (-> {:ppu (ppu/init)}
                (bus/write! 0x8000 0x3C) (bus/write! 0x8010 0x3C)
                (bus/write! 0x8001 0x7E) (bus/write! 0x8011 0x7E)
                (bus/write! 0x8002 0x42) (bus/write! 0x8012 0x42)
                (bus/write! 0x8003 0x42) (bus/write! 0x8013 0x42)
                (bus/write! 0x8004 0x42) (bus/write! 0x8014 0x42)
                (bus/write! 0x8005 0x42) (bus/write! 0x8015 0x42)
                (bus/write! 0x8006 0x42) (bus/write! 0x8016 0x42)
                (bus/write! 0x8007 0x42) (bus/write! 0x8017 0x42)
                (bus/write! 0x8008 0x7E) (bus/write! 0x8018 0x7E)
                (bus/write! 0x8009 0x5E) (bus/write! 0x8019 0x5E)
                (bus/write! 0x800A 0x7E) (bus/write! 0x801A 0x7E)
                (bus/write! 0x800B 0x0A) (bus/write! 0x801B 0x0A)
                (bus/write! 0x800C 0x7C) (bus/write! 0x801C 0x7C)
                (bus/write! 0x800D 0x56) (bus/write! 0x801D 0x56)
                (bus/write! 0x800E 0x38) (bus/write! 0x801E 0x38)
                (bus/write! 0x800F 0x7C) (bus/write! 0x801F 0x7C))

         pixels-v (sut/all-tile-px ctx 4 0x8000 2)
         ;; pixels-v (sut/tile-px ctx {:tile-num 0, :x 0, :y 0, :addr 0x8000, :scale 4})
         pixels   (set pixels-v)
         row0-tile0 #{{:color 0, :x 0, :y 0}
                      {:color 2, :x 4, :y 0}
                      {:color 3, :x 8, :y 0}
                      {:color 3, :x 12, :y 0}
                      {:color 3, :x 16, :y 0}
                      {:color 3, :x 20, :y 0}
                      {:color 2, :x 24, :y 0}
                      {:color 0, :x 28, :y 0}}

         row0-tile1 #{{:color 0, :x 32, :y 0}
                      {:color 2, :x 36, :y 0}
                      {:color 3, :x 40, :y 0}
                      {:color 3, :x 44, :y 0}
                      {:color 3, :x 48, :y 0}
                      {:color 3, :x 52, :y 0}
                      {:color 2, :x 56, :y 0}
                      {:color 0, :x 60, :y 0}}

         row1-tile0 #{{:color 0, :x 0, :y 4}
                      {:color 3, :x 4, :y 4}
                      {:color 0, :x 8, :y 4}
                      {:color 0, :x 12, :y 4}
                      {:color 0, :x 16, :y 4}
                      {:color 0, :x 20, :y 4}
                      {:color 3, :x 24, :y 4}
                      {:color 0, :x 28, :y 4}}

         row1-tile1 #{{:color 0, :x 32, :y 4}
                      {:color 3, :x 36, :y 4}
                      {:color 0, :x 40, :y 4}
                      {:color 0, :x 44, :y 4}
                      {:color 0, :x 48, :y 4}
                      {:color 0, :x 52, :y 4}
                      {:color 3, :x 56, :y 4}
                      {:color 0, :x 60, :y 4}}

         row2-tile0 #{{:color 0, :x 0, :y 8}
                      {:color 3, :x 4, :y 8}
                      {:color 0, :x 8, :y 8}
                      {:color 0, :x 12, :y 8}
                      {:color 0, :x 16, :y 8}
                      {:color 0, :x 20, :y 8}
                      {:color 3, :x 24, :y 8}
                      {:color 0, :x 28, :y 8}}

         row2-tile1 #{{:color 0, :x 32, :y 8}
                      {:color 3, :x 36, :y 8}
                      {:color 0, :x 40, :y 8}
                      {:color 0, :x 44, :y 8}
                      {:color 0, :x 48, :y 8}
                      {:color 0, :x 52, :y 8}
                      {:color 3, :x 56, :y 8}
                      {:color 0, :x 60, :y 8}}

         row3-tile0 #{{:color 0, :x 0, :y 12}
                      {:color 3, :x 4, :y 12}
                      {:color 0, :x 8, :y 12}
                      {:color 0, :x 12, :y 12}
                      {:color 0, :x 16, :y 12}
                      {:color 0, :x 20, :y 12}
                      {:color 3, :x 24, :y 12}
                      {:color 0, :x 28, :y 12}}

         row3-tile1 #{{:color 0, :x 32, :y 12}
                      {:color 3, :x 36, :y 12}
                      {:color 0, :x 40, :y 12}
                      {:color 0, :x 44, :y 12}
                      {:color 0, :x 48, :y 12}
                      {:color 0, :x 52, :y 12}
                      {:color 3, :x 56, :y 12}
                      {:color 0, :x 60, :y 12}}

        row4-tile0 #{{:color 0, :x 0, :y 16}
                     {:color 3, :x 4, :y 16}
                     {:color 1, :x 8, :y 16}
                     {:color 3, :x 12, :y 16}
                     {:color 3, :x 16, :y 16}
                     {:color 3, :x 20, :y 16}
                     {:color 3, :x 24, :y 16}
                     {:color 0, :x 28, :y 16}}

         row4-tile1 #{{:color 0, :x 32, :y 16}
                      {:color 3, :x 36, :y 16}
                      {:color 1, :x 40, :y 16}
                      {:color 3, :x 44, :y 16}
                      {:color 3, :x 48, :y 16}
                      {:color 3, :x 52, :y 16}
                      {:color 3, :x 56, :y 16}
                      {:color 0, :x 60, :y 16}}

        row5-tile0 #{{:color 0, :x 0, :y 20}
                     {:color 1, :x 4, :y 20}
                     {:color 1, :x 8, :y 20}
                     {:color 1, :x 12, :y 20}
                     {:color 3, :x 16, :y 20}
                     {:color 1, :x 20, :y 20}
                     {:color 3, :x 24, :y 20}
                     {:color 0, :x 28, :y 20}}

        row5-tile1 #{{:color 0, :x 32, :y 20}
                     {:color 1, :x 36, :y 20}
                     {:color 1, :x 40, :y 20}
                     {:color 1, :x 44, :y 20}
                     {:color 3, :x 48, :y 20}
                     {:color 1, :x 52, :y 20}
                     {:color 3, :x 56, :y 20}
                     {:color 0, :x 60, :y 20}}

        row6-tile0 #{{:color 0, :x 0, :y 24}
                     {:color 3, :x 4, :y 24}
                     {:color 1, :x 8, :y 24}
                     {:color 3, :x 12, :y 24}
                     {:color 1, :x 16, :y 24}
                     {:color 3, :x 20, :y 24}
                     {:color 2, :x 24, :y 24}
                     {:color 0, :x 28, :y 24}}

        row6-tile1 #{{:color 0, :x 32, :y 24}
                     {:color 3, :x 36, :y 24}
                     {:color 1, :x 40, :y 24}
                     {:color 3, :x 44, :y 24}
                     {:color 1, :x 48, :y 24}
                     {:color 3, :x 52, :y 24}
                     {:color 2, :x 56, :y 24}
                     {:color 0, :x 60, :y 24}}

        row7-tile0 #{{:color 0, :x 0, :y 28}
                     {:color 2, :x 4, :y 28}
                     {:color 3, :x 8, :y 28}
                     {:color 3, :x 12, :y 28}
                     {:color 3, :x 16, :y 28}
                     {:color 2, :x 20, :y 28}
                     {:color 0, :x 24, :y 28}
                     {:color 0, :x 28, :y 28}}

        row7-tile1 #{{:color 0, :x 32, :y 28}
                     {:color 2, :x 36, :y 28}
                     {:color 3, :x 40, :y 28}
                     {:color 3, :x 44, :y 28}
                     {:color 3, :x 48, :y 28}
                     {:color 2, :x 52, :y 28}
                     {:color 0, :x 56, :y 28}
                     {:color 0, :x 60, :y 28}}]

    (is (= (* 8 8 2) (count pixels)))
    (is (= (* 8 8 2) (count pixels-v)))
    (is (set/subset? row0-tile0 pixels))
    (is (set/subset? row0-tile1 pixels))
    (is (set/subset? row1-tile0 pixels))
    (is (set/subset? row1-tile1 pixels))
    (is (set/subset? row2-tile0 pixels))
    (is (set/subset? row2-tile1 pixels))
    (is (set/subset? row3-tile0 pixels))
    (is (set/subset? row3-tile1 pixels))
    (is (set/subset? row4-tile0 pixels))
    (is (set/subset? row4-tile1 pixels))
    (is (set/subset? row5-tile0 pixels))
    (is (set/subset? row5-tile1 pixels))
    (is (set/subset? row6-tile0 pixels))
    (is (set/subset? row6-tile1 pixels))
    (is (set/subset? row7-tile0 pixels))
    (is (set/subset? row7-tile1 pixels))))


(comment
  (def px [])

  (sort-by #(vector (:x %) (:y %)) px)

 ,,,)
