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
    // Don't set a new active window if the current one is offscreen (so it can be restored)
    if (activeWindow != window && isWindowOffscreen(activeWindow)) return;

    const bounds = window.frameGeometry;
    callDBus(
        busName, clientPath, interfaceName,
        "setActiveWindow",
        window.internalId.toString(),
        window.caption,
        bounds.x, bounds.y,
        bounds.width, bounds.height,
        () => {}
    );
}

function resetActiveWindow() {
    if (isWindowOffscreen(activeWindow)) return;

    callDBus(
        busName, clientPath, interfaceName,
        "resetActiveWindow",
        () => {}
    );
}

function move() {
    callDBus(
        busName, clientPath, interfaceName,
        "getWindowPosition",
        (response) => {
            if (!response) return;

            const windowId = response.windowId;
            const x = response.x;
            const y = response.y;

            workspace.windowList().forEach(window => {
                if (window.internalId.toString() == windowId) {
                    const bounds = window.frameGeometry;
                    window.frameGeometry = {
                        x: x,
                        y: y,
                        width: bounds.width,
                        height: bounds.height
                    };
                }
            });
        }
    );
}

function onWindowActivated(window) {
    if (isWindowOffscreen(activeWindow)) return;

    if (!window || !window.normalWindow || window.minimized) {
        if (activeWindow) {
            onWindowDeactivated();
        }
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
    // Reset the active window if it was moved by the user
    // Otherwise the Shimeji will stand in midair
    if (window.move) {
        onWindowActivated(null);
    }
}

function isWindowOffscreen(window) {
    if (!window || !window.normalWindow) return false;

    const windowBounds = window.frameGeometry;
    const screenBounds = workspace.clientArea(KWin.MaximizeArea, window);
    const onScreen =
        windowBounds.x >= screenBounds.x &&
        windowBounds.y >= screenBounds.y &&
        (windowBounds.x + windowBounds.width) <= (screenBounds.x + screenBounds.width) &&
        (windowBounds.y + windowBounds.height) <= (screenBounds.y + screenBounds.height);

    return !onScreen;
}

workspace.windowActivated.connect(onWindowActivated);
onWindowActivated(workspace.activeWindow);

// Start a timer to call move() every 40 milliseconds
const timer = new QTimer();
timer.interval = 40;
timer.timeout.connect(move);
timer.start();
