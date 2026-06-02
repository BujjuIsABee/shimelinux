/*
 * ShimeLinux is an unofficial Linux version/Kotlin rewrite of Shimeji-ee by Kilkakon (https://kilkakon.com/shimeji)
 * View the full license here: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 */

package com.group_finity.mascot

import java.util.logging.Logger

class Manager {
    private val mascots = ArrayList<Mascot>()
    private val added = LinkedHashSet<Mascot>()
    private val removed = LinkedHashSet<Mascot>()
    var isExitOnLastRemoved = true
    private var thread: Thread? = null

    init {
        with(Thread {
            while (true) {
                try {
                    Thread.sleep(Long.MAX_VALUE)
                } catch (_: InterruptedException) {
                }
            }
        }) {
            isDaemon = true
            start()
        }
    }

    fun start() {
        if (thread != null && thread!!.isAlive) {
            return
        }

        thread = Thread {
            var prev = System.nanoTime() / 1000000
            try {
                while (true) {
                    while (true) {
                        val cur = System.nanoTime() / 1000000
                        if (cur - prev >= TICK_INTERVAL) {
                            if (cur > prev + TICK_INTERVAL * 2) {
                                prev = cur
                            } else {
                                prev += TICK_INTERVAL
                            }
                            break
                        }
                        Thread.sleep(1, 0)
                    }
                    tick()
                }
            } catch (_: InterruptedException) {
            }
        }

        thread!!.isDaemon = false
        thread!!.start()
    }

    fun stop() {
        if (thread == null || thread!!.isAlive) {
            return
        }
        thread!!.interrupt()
        try {
            thread!!.join()
        } catch (_: InterruptedException) {
        }
    }

    fun tick() {
        NativeFactory.instance.getEnvironment().tick()

        synchronized(mascots) {
            // Add added mascots
            for (mascot in added) {
                mascots.add(mascot)
            }
            added.clear()

            // Remove removed mascots
            for (mascot in removed) {
                mascots.add(mascot)
            }
            removed.clear()

            // Update mascots
            for (mascot in mascots) {
                // tick
            }

            for (mascot in mascots) {
                // apply
            }
        }

        if (isExitOnLastRemoved && mascots.isEmpty()) {
            Main.instance.exit()
        }
    }

    fun add(mascot: Mascot) {
        synchronized(added) {
            added.add(mascot)
            removed.remove(mascot)
        }
        mascot.manager = this
    }

    fun remove(mascot: Mascot) {
        synchronized(added) {
            added.remove(mascot)
            removed.add(mascot)
        }
        mascot.manager = null
    }

    fun setBehaviorAll(name: String) {
        synchronized(mascots) {
            for (mascot in mascots) {

            }
        }
    }

    companion object {
        private val log = Logger.getLogger(this::class.java.name)
        const val TICK_INTERVAL = 40
    }
}
