(ns gbemu.ppu)

(defn init []
  {:oam-ram (apply vector-of :byte (repeat 160 0))
   :vram    (apply vector-of :byte (repeat 0x2000 0))})


(defn y-flip? [])
(defn x-flip? [])
(defn palette-num [])


;; typedef struct {
;;                 oam_entry oam_ram[40];
;;                 u8 vram[0x2000]};
;;  ppu_context;

;; void ppu_init();
;; void ppu_tick();

;; void ppu_oam_write(u16 address, u8 value);
;; u8 ppu_oam_read(u16 address);

;; void ppu_vram_write(u16 address, u8 value);
;; u8 ppu_vram_read(u16 address);
(defn tick [])

(defn read-oam [ctx addr]
  (let [address (if (< 0xFE00 addr) (- addr 0xFE00) addr)]
    (get-in ctx [:ppu :oam-ram address])))

(defn write-oam [ctx addr value]
  (let [address (if (< 0xFE00 addr) (- addr 0xFE00) addr)]
    (update-in ctx [:ppu :oam-ram address] value)))

(defn read-vram [ctx addr]
  (let [address (- addr 0x8000)]
    (get-in ctx [:ppu :vram address])))

(defn write-vram [ctx addr value]
  (let [address (- addr 0x8000)]
    (update-in ctx [:ppu :vram address])))
