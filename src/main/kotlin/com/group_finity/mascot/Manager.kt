/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot

import com.group_finity.mascot.config.Configuration
import com.group_finity.mascot.exception.BehaviorInstantiationException
import com.group_finity.mascot.exception.CantBeAliveException
import java.awt.Point
import java.lang.ref.WeakReference
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.concurrent.thread

class Manager {
    private val mascots = ArrayList<Mascot>()
    private val added = LinkedHashSet<Mascot>()
    private val removed = LinkedHashSet<Mascot>()
    var isExitOnLastRemoved = true
    private var thread: Thread? = null

    val isPaused: Boolean
        get() {
            var isPaused = true
            synchronized(mascots) {
                for (mascot in mascots) {
                    if (!mascot.isPaused) {
                        isPaused = false
                        break
                    }
                }
            }
            return isPaused
        }

    val count: Int
        get() = getCount(null)

    init {
        thread(start = true, isDaemon = true)  {
            while (true) {
                try {
                    Thread.sleep(Long.MAX_VALUE)
                } catch (_: InterruptedException) {
                }
            }
        }
    }

    fun start() {
        if (thread != null && thread!!.isAlive) return

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
        if (thread == null || !thread!!.isAlive) return

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
                mascot.tick()
            }

            // Animate mascots
            for (mascot in mascots) {
                mascot.apply()
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
                try {
                    val configuration = checkNotNull(Main.instance.getConfiguration(mascot.imageSet))
                    mascot.behavior = configuration.buildBehavior(configuration.schema.getString(name), mascot)
                } catch (e: BehaviorInstantiationException) {
                    log.log(Level.SEVERE, "Failed to set behavior.", e)
                    Main.showError(Main.instance.languageBundle.getString("FailedSetBehaviourErrorMessage"), e)
                    mascot.dispose()
                } catch (e: CantBeAliveException) {
                    log.log(Level.SEVERE, "Fatal Error", e)
                    Main.showError(Main.instance.languageBundle.getString("FailedSetBehaviourErrorMessage"), e)
                    mascot.dispose()
                }
            }
        }
    }

    fun setBehaviorAll(configuration: Configuration, name: String, imageSet: String) {
        synchronized(mascots) {
            for (mascot in mascots) {
                try {
                    if (mascot.imageSet == imageSet) {
                        mascot.behavior = configuration.buildBehavior(configuration.schema.getString(name), mascot)
                    }
                } catch (e: BehaviorInstantiationException) {
                    log.log(Level.SEVERE, "Failed to set behavior.", e)
                    Main.showError(Main.instance.languageBundle.getString("FailedSetBehaviourErrorMessage"), e)
                    mascot.dispose()
                } catch (e: CantBeAliveException) {
                    log.log(Level.SEVERE, "Fatal Error", e)
                    Main.showError(Main.instance.languageBundle.getString("FailedSetBehaviourErrorMessage"), e)
                    mascot.dispose()
                }
            }
        }
    }

    fun remainOne() {
        synchronized(mascots) {
            for (i in mascots.size - 1 downTo 0) {
                mascots[i].dispose()
            }
        }
    }

    fun remainOne(mascot: Mascot) {
        synchronized(mascots) {
            for (i in mascots.size - 1 downTo 0) {
                if (mascots[i] != mascot) {
                    mascots[i].dispose()
                }
            }
        }
    }

    fun remainOne(imageSet: String) {
        synchronized(mascots) {
            var isFirst = true
            for (i in mascots.size - 1 downTo 0) {
                val mascot = mascots[i]
                if (mascot.imageSet == imageSet) {
                    if (isFirst) {
                        isFirst = false
                    } else {
                        mascot.dispose()
                    }
                }
            }
        }
    }

    fun remainNone(imageSet: String) {
        synchronized(mascots) {
            for (i in mascots.size - 1 downTo 0) {
                val mascot = mascots[i]
                if (mascot.imageSet == imageSet) {
                    mascot.dispose()
                }
            }
        }
    }

    fun togglePauseAll() {
        synchronized(mascots) {
            val isPaused = this.isPaused
            for (mascot in mascots) {
                mascot.isPaused = !isPaused
            }
        }
    }

    fun getCount(imageSet: String?): Int {
        synchronized(mascots) {
            if (imageSet == null) return mascots.size

            var result = 0
            for (mascot in mascots) {
                if (mascot.imageSet == imageSet) {
                    result++
                }
            }
            return result
        }
    }

    fun getMascotWithAffordance(affordance: String): WeakReference<Mascot>? {
        synchronized(mascots) {
            for (mascot in mascots) {
                if (mascot.affordances.contains(affordance)) {
                    return WeakReference(mascot)
                }
            }
        }
        return null
    }

    fun hasOverlappingMascotsAtPoint(anchor: Point): Boolean {
        synchronized(mascots) {
            var count = 0
            for (mascot in mascots) {
                if (mascot.anchor == anchor) {
                    count++
                }
                if (count > 1) {
                    return true
                }
            }
        }
        return false
    }

    fun disposeAll() {
        synchronized(mascots) {
            for (i in mascots.size - 1 downTo 0) {
                mascots[i].dispose()
            }
        }
    }

    companion object {
        private val log = Logger.getLogger(this::class.java.name)
        const val TICK_INTERVAL = 40
    }
}
