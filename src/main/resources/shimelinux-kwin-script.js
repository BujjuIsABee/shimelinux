/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
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
