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

function setActiveWindow(window) {
    callDBus(
        busName, clientPath, interfaceName,
        "setActiveWindow",
        window.caption,
        window.x, window.y,
        window.width, window.height,
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

function update() {
    // Move windows
    callDBus(
        busName, clientPath, interfaceName,
        "getWindowPosition",
        (windowPosition) => {
            if (!windowPosition || !activeWindow) return;

            activeWindow.frameGeometry = {
                x: windowPosition.x,
                y: windowPosition.y,
                width: activeWindow.width,
                height: activeWindow.height
            };
        }
    );

    // Restore windows
    callDBus(
        busName, clientPath, interfaceName,
        "getRestoreWindows",
        (restoreWindows) => {
            if (!restoreWindows) return;

            const windows = workspace.windowList();
            for (const window of windows) {
                if (!window || !window.normalWindow || isWindowOnscreen(window)) continue;

                const screen = workspace.clientArea(KWin.MaximizeArea, window);
                const centerX = (screen.width / 2) - (window.width / 2);
                const centerY = (screen.height / 2) - (window.height / 2);

                window.frameGeometry = {
                    x: centerX,
                    y: centerY,
                    width: window.width,
                    height: window.height
                };
            }
        }
    );
}

function onWindowActivated(window) {
    if (!window || !window.normalWindow || window.minimized) {
        onWindowDeactivated();
        return;
    }

    setActiveWindow(window);

    if (activeWindow != window) {
        activeWindow = window;
        window.frameGeometryChanged.connect(setActiveWindow.bind(null, window));
        window.closed.connect(resetActiveWindow.bind(null));
        window.minimizedChanged.connect(resetActiveWindow.bind(null));
        window.moveResizedChanged.connect(resetActiveWindow.bind(null));
    }
}

function onWindowDeactivated() {
    resetActiveWindow();
}

function isWindowOnscreen(window) {
    const screen = workspace.clientArea(KWin.MaximizeArea, window);

    return window.y + window.height >= screen.y &&
        window.x + window.width >= screen.x &&
        window.y <= screen.y + screen.height &&
        window.x <= screen.x + screen.width;
}

workspace.windowActivated.connect(onWindowActivated);

// Start a timer to call update() every 40 milliseconds
const timer = new QTimer();
timer.interval = 40;
timer.timeout.connect(update);
timer.start();
