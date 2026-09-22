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

package com.group_finity.mascot

import com.group_finity.mascot.config.Configuration
import com.group_finity.mascot.exception.BehaviorInstantiationException
import com.group_finity.mascot.exception.CantBeAliveException
import java.awt.Point
import java.lang.ref.WeakReference
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.concurrent.timer

private val logger: Logger = Logger.getLogger(Manager::class.java.name)

/**
 * Manages a list of mascots.
 *
 * @author Yuki Yamada
 * @author Kilkakon
 * @author Bujju
 */
class Manager {
    private val mascots = mutableListOf<Mascot>()
    private val added = linkedSetOf<Mascot>()
    private val removed = linkedSetOf<Mascot>()
    private var timer: Timer? = null

    /**
     * Whether the program should be closed if the list is empty.
     */
    var isExitOnLastRemoved = true

    /**
     * Whether all mascots are paused.
     */
    val isPaused: Boolean
        get() = synchronized(mascots) { mascots.none { !it.isPaused } }

    /**
     * The total number of mascots in the list.
     */
    val count: Int
        get() = getCount(null)

    /**
     * Starts dispatching ticks to the mascots in the list.
     */
    fun start() {
        if (timer == null) {
            timer = timer(daemon = false, period = 40L) { tick() }
        }
    }

    /**
     * Stops ticks from being dispatched to the mascots in the list.
     */
    fun stop() {
        timer?.let {
            it.cancel()
            timer = null
        }
    }

    /**
     * Dispatches a tick to the mascots in the list.
     */
    fun tick() {
        NativeFactory.instance.environment.tick()

        synchronized(mascots) {
            // Add added mascots
            for (mascot in added) {
                mascots.add(mascot)
            }
            added.clear()

            // Remove removed mascots
            for (mascot in removed) {
                mascots.remove(mascot)
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
            Main.exit()
        }
    }

    /**
     * Adds [mascot] to the list on the next [tick].
     */
    fun add(mascot: Mascot) {
        synchronized(added) {
            added.add(mascot)
            removed.remove(mascot)
        }
        mascot.manager = this
    }

    /**
     * Removes [mascot] from the list on the next [tick].
     */
    fun remove(mascot: Mascot) {
        synchronized(added) {
            added.remove(mascot)
            removed.add(mascot)
        }
        mascot.manager = null
    }

    /**
     * Sets the behavior to [name] for all mascots.
     */
    fun setBehaviorAll(name: String) {
        synchronized(mascots) {
            for (mascot in mascots) {
                try {
                    val configuration = Main.getConfiguration(mascot.imageSet)
                    mascot.behavior = configuration.buildBehavior(configuration.schema.getString(name), mascot)
                } catch (e: Exception) {
                    when (e) {
                        is BehaviorInstantiationException,
                        is CantBeAliveException -> {
                            logger.log(Level.SEVERE, e) { "Failed to set behavior." }
                            Main.showError(localize("FailedSetBehaviorErrorMessage"), e)
                            mascot.dispose()
                        }

                        else -> throw e
                    }
                }
            }
        }
    }

    /**
     * Sets the behavior to [name] for all mascots with the [imageSet].
     */
    fun setBehaviorAll(configuration: Configuration, name: String, imageSet: String) {
        synchronized(mascots) {
            for (mascot in mascots) {
                try {
                    if (mascot.imageSet == imageSet) {
                        mascot.behavior = configuration.buildBehavior(configuration.schema.getString(name), mascot)
                    }
                } catch (e: Exception) {
                    when (e) {
                        is BehaviorInstantiationException,
                        is CantBeAliveException -> {
                            logger.log(Level.SEVERE, e) { "Failed to set behavior ($name)" }
                            Main.showError(localize("FailedSetBehaviorErrorMessage"), e)
                            mascot.dispose()
                        }

                        else -> throw e
                    }

                }
            }
        }
    }

    /**
     * Dismisses all mascots but one.
     */
    fun remainOne() {
        synchronized(mascots) {
            val totalMascots = mascots.size
            for (i in totalMascots - 1 downTo 1) {
                mascots[i].dispose()
            }
        }
    }

    /**
     * Dismisses all mascots except [mascot].
     */
    fun remainOne(mascot: Mascot) {
        synchronized(mascots) {
            val totalMascots = mascots.size
            for (i in totalMascots - 1 downTo 0) {
                if (mascots[i] != mascot) {
                    mascots[i].dispose()
                }
            }
        }
    }

    /**
     * Dismisses all mascots except one with the [imageSet].
     */
    fun remainOne(imageSet: String) {
        synchronized(mascots) {
            var isFirst = true
            val totalMascots = mascots.size
            for (i in totalMascots - 1 downTo 0) {
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

    /**
     * Dismisses all mascots with the [imageSet].
     */
    fun remainNone(imageSet: String) {
        synchronized(mascots) {
            val totalMascots = mascots.size
            for (i in totalMascots - 1 downTo 0) {
                val mascot = mascots[i]
                if (mascot.imageSet == imageSet) {
                    mascot.dispose()
                }
            }
        }
    }

    /**
     * Sets all [Mascot.isPaused] to [isPaused] for all mascots.
     */
    fun togglePauseAll() {
        synchronized(mascots) {
            val isPaused = isPaused
            for (mascot in mascots) {
                mascot.isPaused = !isPaused
            }
        }
    }

    /**
     * Gets the number of mascots with the [imageSet].
     */
    fun getCount(imageSet: String?): Int = synchronized(mascots) {
        if (imageSet != null) {
            mascots.count { it.imageSet == imageSet }
        } else {
            mascots.size
        }
    }

    /**
     * Gets the first mascot with the [affordance].
     */
    fun getMascotWithAffordance(affordance: String): WeakReference<Mascot>? = synchronized(mascots) {
        mascots.firstOrNull { it.affordances.contains(affordance) }?.let {
            WeakReference(it)
        }
    }

    /**
     * Whether there is more than one mascot at [anchor].
     */
    fun hasOverlappingMascotsAtPoint(anchor: Point): Boolean = synchronized(mascots) {
        mascots.count { it.anchor == anchor } > 1
    }

    /**
     * Dismisses all mascots.
     */
    fun disposeAll() {
        synchronized(mascots) {
            val totalMascots = mascots.size
            for (i in totalMascots - 1 downTo 0) {
                mascots[i].dispose()
            }
        }
    }
}
