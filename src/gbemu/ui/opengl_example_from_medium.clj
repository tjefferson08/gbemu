(ns gbemu.ui.opengl-example-from-medium
  (:gen-class)
  (:import (org.lwjgl Version)
           (org.lwjgl.glfw GLFWErrorCallback GLFW GLFWKeyCallbackI Callbacks)
           (org.lwjgl.opengl GL GL33)
           (org.lwjgl.system MemoryStack))
  (:require [gbemu.log :as log]
            [gbemu.state :refer [*ctx]]
            [gbemu.bus :as bus]))

; embedded nREPL
(require '[nrepl.server :refer [start-server stop-server]])
(defonce server (start-server :port 7888))

; The window handle
(declare window dbg-window)

; forward references
(declare init init-window main-loop draw draw-dbg)

(def SCREEN_WIDTH 1024)
(def SCREEN_HEIGHT 768)
(def SCREEN_X 0)
(def SCREEN_Y 0)
(def SCALE 4)

(defn run
  ([] (run nil))
  ([wait-ms]

   (println (str "Hello LWJGL " (Version/getVersion) "!"))

   (init)
   (def window (init-window SCREEN_WIDTH SCREEN_HEIGHT SCREEN_X SCREEN_Y))
   (def dbg-window (init-window (* 16 8 SCALE) (* 32 8 SCALE) (+ SCREEN_X SCREEN_WIDTH 10) SCREEN_Y))             ; Center the window
   (main-loop wait-ms)

   ; Free the window callbacks and destroy the window
   (Callbacks/glfwFreeCallbacks window)
   (GLFW/glfwDestroyWindow window)

   ; Terminate GLFW and free the error callback
   (GLFW/glfwTerminate)
   (-> (GLFW/glfwSetErrorCallback nil) (.free))))

(defn -main [& args]
  (apply run args))

(defn init []
  ; Setup an error callback. The default implementation
  ; will print the error message in System.err.
  (-> (GLFWErrorCallback/createPrint System/err) (.set))

  ; Initialize GLFW. Most GLFW functions will not work before doing this.
  (when (not (GLFW/glfwInit))
    (throw (IllegalStateException. "Unable to initialize GLFW")))

  ; Configure GLFW
  (GLFW/glfwDefaultWindowHints)                             ; optional, the current window hints are already the default
  (GLFW/glfwWindowHint GLFW/GLFW_VISIBLE GLFW/GLFW_FALSE)   ; the window will stay hidden after creation
  (GLFW/glfwWindowHint GLFW/GLFW_RESIZABLE GLFW/GLFW_TRUE))  ; the window will be resizable

(defn init-window
 ([] (init-window SCREEN_HEIGHT SCREEN_WIDTH nil nil))
 ([width height xpos ypos]
   ; Create the window
  (let [win (GLFW/glfwCreateWindow width height "Hello, World!" 0 0)]
    (when (zero? win)
      (throw (RuntimeException. "Failed to create the GLFW window")))

    ; Setup a key callback. It will be called every time a key is pressed, repeated or released.
    (GLFW/glfwSetKeyCallback win (reify GLFWKeyCallbackI
                                     (invoke [this wndow key scancode action mods]
                                       (when (and (= key GLFW/GLFW_KEY_ESCAPE)
                                               (= action GLFW/GLFW_RELEASE))
                                           ; We will detect this in the rendering loop
                                           (GLFW/glfwSetWindowShouldClose wndow true)))))

    ; Get the thread stack and push a new frame
    (let [stack (MemoryStack/stackPush)
            p-width (.mallocInt stack 1)
            p-height (.mallocInt stack 1)]

        ; Get the window size passed to glfwCreateWindow
        (GLFW/glfwGetWindowSize ^long win p-width p-height)
        (let [vidmode (-> (GLFW/glfwGetPrimaryMonitor)          ; Get the resolution of the primary monitor
                          (GLFW/glfwGetVideoMode))
              xpos' (or xpos (/ (- (.width vidmode)
                                 (.get p-width 0))
                              2))
              ypos' (or ypos (/ (- (.height vidmode)
                                  (.get p-height 0))
                               2))]
          (GLFW/glfwSetWindowPos win xpos' ypos')             ; Center the window
          (MemoryStack/stackPop)))                                  ; pop stack frame


    (GLFW/glfwMakeContextCurrent win)                      ; Make the OpenGL context current
    (GLFW/glfwSwapInterval 1)                                 ; Enable v-sync
    (GLFW/glfwShowWindow win)                             ; Make the window visible
   win)))

(defn main-loop [wait]
  ; This line is critical for LWJGL's interoperation with GLFW's
  ; OpenGL context, or any context that is managed externally.
  ; LWJGL detects the context that is current in the current thread,
  ; creates the GLCapabilities instance and makes the OpenGL
  ; bindings available for use.
  (GL/createCapabilities)

  ; Set the clear color
  (GL33/glClearColor 1.0 0.0 0.0 0.0)

  ; Run the rendering loop until the user has attempted to close
  ; the window or has pressed the ESCAPE key.
  (while (not (GLFW/glfwWindowShouldClose window))
    (and wait (Thread/sleep wait))
    (draw window)
    (draw-dbg dbg-window)))



    ;; for (int tileY=0; tileY<16; tileY += 2) {
    ;;     u8 b1 = bus_read(startLocation + (tileNum * 16) + tileY);
    ;;     u8 b2 = bus_read(startLocation + (tileNum * 16) + tileY + 1);

    ;;     for (int bit=7; bit >= 0; bit--) {
    ;;         u8 hi = !!(b1 & (1 << bit)) << 1;
    ;;         u8 lo = !!(b2 & (1 << bit));

    ;;         u8 color = hi | lo;

    ;;         rc.x = x + ((7 - bit) * scale);
    ;;         rc.y = y + (tileY / 2 * scale);
    ;;         rc.w = scale;
    ;;         rc.h = scale;

    ;;         SDL_FillRect(surface, &rc, tile_colors[color]);
    ;;     }
    ;; }
    ;; static unsigned long tile_colors[4] = {0xFFFFFFFF, 0xFFAAAAAA, 0xFF555555, 0xFF000000};
(def TILE_COLORS [[1.0 1.0 1.0 1.0] [1.0 0.625 0.625 0.625] [1.0 0.3125 0.2135 0.3125] [1.0 0 0 0]])

(defn tile-px [ctx {:keys [tile-num x y addr scale]}]
  (let [tile-line (fn [b1 b2 tile-y]
                     (map (fn [bit]
                            (let [hi    (if (bit-test b2 bit) 2r10 2r00)
                                  lo    (if (bit-test b1 bit) 2r01 2r00)
                                  color (bit-or hi lo)
                                  x     (+ x (* (- 7 bit) scale))
                                  y     (* tile-y scale)
                                  _     (println (format (str "bytes 1:%02X 2:%02X color: " color " hi: " hi " lo: " lo " x, y: " x "," y) b1 b2))]
                               {:color color, :x x, :y y}))
                          (range 0 8)))]
    (mapcat (fn [line] (let [tile-y (* 2 line)
                             b1     (bus/read! ctx (+ addr (* tile-num 16) tile-y))
                             b2     (bus/read! ctx (+ addr (* tile-num 16) (inc tile-y)))]
                          (tile-line b1 b2 line)))
         (range 0 8))))

(defn draw-dbg [win]
  (GLFW/glfwMakeContextCurrent win)                      ; Make the OpenGL context current
  (GL33/glClearColor 0.5 0.5 0.5 0)

    ;; u16 addr = 0x8000;

    ;; //384 tiles, 24 x 16
    ;; for (int y=0; y<24; y++) {
    ;;     for (int x=0; x<16; x++) {
    ;;         display_tile(debugScreen, addr, tileNum, xDraw + (x * scale), yDraw + (y * scale));
    ;;         xDraw += (8 * scale);
    ;;         tileNum++;
    ;;     }

    ;;     yDraw += (8 * scale);
    ;;     xDraw = 0;
    ;; }
  (let [tiles (for [x (range 8)
                    y (range 12)]
                (tile-px @*ctx {:scale SCALE, :addr 0x8000, :tile-num (+ x (* 16 y)), :x (* x SCALE), :y (* y SCALE)}))]
    (doall (take 2 tiles)))


  ; clear the framebuffer
  (GL33/glClear (bit-or GL33/GL_COLOR_BUFFER_BIT GL33/GL_DEPTH_BUFFER_BIT))
;; void glDrawPixels(                                  	GLsizei width,
;;                                    	GLsizei height,
;;                                    	GLenum format,
;;                                    	GLenum type,
;;                                    	const void * data);


  ; swap the color buffers
  (GLFW/glfwSwapBuffers win)

  ; Poll for window events. The key callback above will only be
  ; invoked during this call.
  (GLFW/glfwPollEvents))

(defn draw [win]
  (GLFW/glfwMakeContextCurrent win)                      ; Make the OpenGL context current
  (GL33/glClearColor 1.0 0.0 0.0 0.0)

  ; clear the framebuffer
  (GL33/glClear (bit-or GL33/GL_COLOR_BUFFER_BIT GL33/GL_DEPTH_BUFFER_BIT))

  (println "hi from draw")
;; void glDrawPixels(                                  	GLsizei width,
;;                                    	GLsizei height,
;;                                    	GLenum format,
;;                                    	GLenum type,
;;                                    	const void * data);


  ; swap the color buffers
  (GLFW/glfwSwapBuffers win)

  ; Poll for window events. The key callback above will only be
  ; invoked during this call.
  (GLFW/glfwPollEvents))

(comment
   (GLFW/glfwSetWindowSize dbg-window (* 16 8 SCALE) (* 32 8 SCALE))             ; Center the window

   (GLFW/glfwSetWindowPos window 300 200)             ; Center the window

   window
   (for [x (range 1)
         y (range 1)]
     [x y (+ x (* 4 y))])

 ,,,)
