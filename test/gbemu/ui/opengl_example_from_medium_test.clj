(ns gbemu.ui.opengl-example-from-medium-test
  (:require [gbemu.ui.opengl-example-from-medium :as sut]
            [clojure.test :refer :all]
            [gbemu.ppu :as ppu]
            [gbemu.bus :as bus]
            [clojure.set :as set]))

(deftest tile-px
  ;; lifted example from Pandocs: https://gbdev.io/pandocs/Tile_Data.html
  (let [ctx (-> {:ppu (ppu/init)}
                (bus/write! 0x8000 0x3C)
                (bus/write! 0x8001 0x7E)
                (bus/write! 0x8002 0x42)
                (bus/write! 0x8003 0x42)
                (bus/write! 0x8004 0x42)
                (bus/write! 0x8005 0x42)
                (bus/write! 0x8006 0x42)
                (bus/write! 0x8007 0x42)
                (bus/write! 0x8008 0x7E)
                (bus/write! 0x8009 0x5E)
                (bus/write! 0x800A 0x7E)
                (bus/write! 0x800B 0x0A)
                (bus/write! 0x800C 0x7C)
                (bus/write! 0x800D 0x56)
                (bus/write! 0x800E 0x38)
                (bus/write! 0x800F 0x7C))
         pixels (-> (sut/tile-px ctx {:tile-num 0, :x 0, :y 0, :addr 0x8000, :scale 4})
                    set)
         row0 #{{:color 0, :x 0, :y 0}
                {:color 2, :x 4, :y 0}
                {:color 3, :x 8, :y 0}
                {:color 3, :x 12, :y 0}
                {:color 3, :x 16, :y 0}
                {:color 3, :x 20, :y 0}
                {:color 2, :x 24, :y 0}
                {:color 0, :x 28, :y 0}}

         row1 #{{:color 0, :x 0, :y 4}
                {:color 3, :x 4, :y 4}
                {:color 0, :x 8, :y 4}
                {:color 0, :x 12, :y 4}
                {:color 0, :x 16, :y 4}
                {:color 0, :x 20, :y 4}
                {:color 3, :x 24, :y 4}
                {:color 0, :x 28, :y 4}}

         row2 #{{:color 0, :x 0, :y 8}
                {:color 3, :x 4, :y 8}
                {:color 0, :x 8, :y 8}
                {:color 0, :x 12, :y 8}
                {:color 0, :x 16, :y 8}
                {:color 0, :x 20, :y 8}
                {:color 3, :x 24, :y 8}
                {:color 0, :x 28, :y 8}}

         row3 #{{:color 0, :x 0, :y 12}
                {:color 3, :x 4, :y 12}
                {:color 0, :x 8, :y 12}
                {:color 0, :x 12, :y 12}
                {:color 0, :x 16, :y 12}
                {:color 0, :x 20, :y 12}
                {:color 3, :x 24, :y 12}
                {:color 0, :x 28, :y 12}}

        row4 #{{:color 0, :x 0, :y 16}
               {:color 3, :x 4, :y 16}
               {:color 1, :x 8, :y 16}
               {:color 3, :x 12, :y 16}
               {:color 3, :x 16, :y 16}
               {:color 3, :x 20, :y 16}
               {:color 3, :x 24, :y 16}
               {:color 0, :x 28, :y 16}}

        row5 #{{:color 0, :x 0, :y 20}
               {:color 1, :x 4, :y 20}
               {:color 1, :x 8, :y 20}
               {:color 1, :x 12, :y 20}
               {:color 3, :x 16, :y 20}
               {:color 1, :x 20, :y 20}
               {:color 3, :x 24, :y 20}
               {:color 0, :x 28, :y 20}}

        row6 #{{:color 0, :x 0, :y 24}
               {:color 3, :x 4, :y 24}
               {:color 1, :x 8, :y 24}
               {:color 3, :x 12, :y 24}
               {:color 1, :x 16, :y 24}
               {:color 3, :x 20, :y 24}
               {:color 2, :x 24, :y 24}
               {:color 0, :x 28, :y 24}}

        row7 #{{:color 0, :x 0, :y 28}
               {:color 2, :x 4, :y 28}
               {:color 3, :x 8, :y 28}
               {:color 3, :x 12, :y 28}
               {:color 3, :x 16, :y 28}
               {:color 2, :x 20, :y 28}
               {:color 0, :x 24, :y 28}
               {:color 0, :x 28, :y 28}}]

    (is (set/subset? row0 pixels))
    (is (set/subset? row1 pixels))
    (is (set/subset? row2 pixels))
    (is (set/subset? row3 pixels))
    (is (set/subset? row4 pixels))
    (is (set/subset? row5 pixels))
    (is (set/subset? row6 pixels))
    (is (set/subset? row7 pixels))))

