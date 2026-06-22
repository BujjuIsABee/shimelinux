/*
 * Copyright (c) 2026, Bujju
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *        following disclaimer.
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *        following disclaimer in the documentation and/or other materials provided with the distribution.
 *     3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote
 *        products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

const busName = "io.github.bujjuisabee.shimelinux";
const clientPath = "/KWinClient";
const interfaceName = "io.github.bujjuisabee.shimelinux";

let activeWindow = null;
let frameGeometryChangedHandler = null;
let windowClosedOrMinimizedHandler = null;
let moveResizedChangedHandler = null;

function setActiveWindow(window) {
    const bounds = window.frameGeometry;
    callDBus(
        busName, clientPath, interfaceName,
        "setActiveWindow",
        window.caption,
        bounds.x, bounds.y,
        bounds.width, bounds.height,
        () => {}
    );
}

function resetActiveWindow() {
    callDBus(
        busName, clientPath, interfaceName,
        "resetActiveWindow",
        () => {}
    );
}

function tick() {
    // Move windows
    callDBus(
        busName, clientPath, interfaceName,
        "getWindowPosition",
        (response) => {
            if (!response || !activeWindow) return;

            const x = response.x;
            const y = response.y;

            const bounds = activeWindow.frameGeometry;
            activeWindow.frameGeometry = {
                x: x,
                y: y,
                width: bounds.width,
                height: bounds.height
            };
        }
    );

    // Restore windows
    callDBus(
        busName, clientPath, interfaceName,
        "getRestoreWindows",
        (response) => {
            if (!response) return;

            const windows = workspace.windowList();
            for (const window of windows) {
                if (!window || !window.normalWindow || isWindowOnscreen(window)) continue;

                const windowBounds = window.frameGeometry;
                const screenBounds = workspace.clientArea(KWin.MaximizeArea, window);
                const centerX = (screenBounds.width / 2) - (windowBounds.width / 2);
                const centerY = (screenBounds.height / 2) - (windowBounds.height / 2);

                window.frameGeometry = {
                    x: centerX,
                    y: centerY,
                    width: windowBounds.width,
                    height: windowBounds.height
                };
            }
        }
    );
}

function onWindowActivated(window) {
    if (!window || !window.normalWindow || window.minimized) {
        if (activeWindow) onWindowDeactivated();
        return;
    }

    setActiveWindow(window);

    if (activeWindow != window) {
        activeWindow = window;

        frameGeometryChangedHandler = setActiveWindow.bind(null, window);
        windowClosedOrMinimizedHandler = resetActiveWindow.bind(null);
        moveResizedChangedHandler = onMoveResizedChanged.bind(null, window);

        window.frameGeometryChanged.connect(frameGeometryChangedHandler);
        window.closed.connect(windowClosedOrMinimizedHandler);
        window.minimizedChanged.connect(windowClosedOrMinimizedHandler);
        window.moveResizedChanged.connect(moveResizedChangedHandler);
    }
}

function onWindowDeactivated() {
    resetActiveWindow();

    activeWindow.frameGeometryChanged.disconnect(frameGeometryChangedHandler);
    activeWindow.closed.disconnect(windowClosedOrMinimizedHandler);
    activeWindow.minimizedChanged.disconnect(windowClosedOrMinimizedHandler);
    activeWindow.moveResizedChanged.disconnect(moveResizedChangedHandler);

    frameGeometryChangedHandler = null;
    windowClosedOrMinimizedHandler = null;
    moveResizedChangedHandler = null;
}

function onMoveResizedChanged(window) {
    // Reset the active window if it was moved by the user so the Shimeji doesn't stand in midair
    if (window.move) onWindowActivated(null);
}

function isWindowOnscreen(window) {
    const windowBounds = window.frameGeometry;
    const screenBounds = workspace.clientArea(KWin.MaximizeArea, window);

    return windowBounds.y + windowBounds.height >= screenBounds.y &&
        windowBounds.x + windowBounds.width >= screenBounds.x &&
        windowBounds.y <= screenBounds.y + screenBounds.height &&
        windowBounds.x <= screenBounds.x + screenBounds.width;
}

workspace.windowActivated.connect(onWindowActivated);
onWindowActivated(workspace.activeWindow);

// Start a timer to call tick() every 40 milliseconds
const timer = new QTimer();
timer.interval = 40;
timer.timeout.connect(tick);
timer.start();
