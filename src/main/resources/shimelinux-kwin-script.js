const busName = "io.github.bujjuisabee.shimelinux";
const clientPath = "/KWinClient";
const interfaceName = "io.github.bujjuisabee.shimelinux";

let activeWindow = null;
let frameGeometryChangedHandler = null;
let windowClosedOrMinimizedHandler = null;
let moveResizedChangedHandler = null;
let width = null;
let height = null;

function setActiveWindow(window) {
    if (activeWindow != window && !isWindowOnscreen(activeWindow)) return;

    const bounds = window.frameGeometry;
    callDBus(
        busName, clientPath, interfaceName,
        "setActiveWindow",
        window.internalId.toString(),
        window.resourceClass,
        bounds.x, bounds.y,
        bounds.width, bounds.height,
        () => {}
    );
}

function resetActiveWindow() {
    if (!isWindowOnscreen(activeWindow)) return;

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
        (params) => {
            if (params[0] == "null" || params[1] == "null" || params[2] == "null") return;

            const windowId = params[0];
            const x = parseInt(params[1], 10);
            const y = parseInt(params[2], 10);

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
    if (!isWindowOnscreen(activeWindow)) return;

    if (!window || !window.normalWindow || window.minimized) {
        onWindowDeactivated();
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
    if (!isWindowOnscreen(activeWindow)) return;

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
    if (window.move) {
        onWindowActivated(null);
    }
}

function isWindowOnscreen(window) {
    if (!window || !window.normalWindow) return true;

    const windowBounds = window.frameGeometry;
    const screenBounds = workspace.clientArea(KWin.MaximizeArea, window);

    return windowBounds.x >= screenBounds.x && windowBounds.y >= screenBounds.y &&
    (windowBounds.x + windowBounds.width) <= (screenBounds.x + screenBounds.width) &&
    (windowBounds.y + windowBounds.height) <= (screenBounds.y + screenBounds.height);
}

workspace.windowActivated.connect(onWindowActivated);

const timer = new QTimer();
timer.interval = 40;
timer.timeout.connect(move);
timer.start();
