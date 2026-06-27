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
import java.util.Timer
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.concurrent.timer

class Manager {
    private val mascots = mutableListOf<Mascot>()
    private val added = linkedSetOf<Mascot>()
    private val removed = linkedSetOf<Mascot>()
    private var timer: Timer? = null
    var isExitOnLastRemoved = true

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
    val count
        get() = getCount(null)

    fun start() {
        if (timer != null) return

        timer = timer("UpdateMascot", false, period = TICK_INTERVAL) { tick() }
    }

    fun stop() {
        timer?.let {
            it.cancel()
            timer = null
        }
    }

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
                    Main.showError(Main.instance.languageBundle.getString("FailedSetBehaviorErrorMessage"), e)
                    mascot.dispose()
                } catch (e: CantBeAliveException) {
                    log.log(Level.SEVERE, "Fatal Error", e)
                    Main.showError(Main.instance.languageBundle.getString("FailedSetBehaviorErrorMessage"), e)
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
                    log.log(Level.SEVERE, "Failed to set behavior ($name)", e)
                    Main.showError(Main.instance.languageBundle.getString("FailedSetBehaviorErrorMessage"), e)
                    mascot.dispose()
                } catch (e: CantBeAliveException) {
                    log.log(Level.SEVERE, "Failed to set behavior ($name)", e)
                    Main.showError(Main.instance.languageBundle.getString("FailedSetBehaviorErrorMessage"), e)
                    mascot.dispose()
                }
            }
        }
    }

    fun remainOne() {
        synchronized(mascots) {
            val totalMascots = mascots.size
            for (i in totalMascots - 1 downTo 0) {
                mascots[i].dispose()
            }
        }
    }

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
                if (mascot.imageSet == imageSet) result++
            }
            return result
        }
    }

    fun getMascotWithAffordance(affordance: String): WeakReference<Mascot>? {
        synchronized(mascots) {
            for (mascot in mascots) {
                if (mascot.affordances.contains(affordance)) return WeakReference(mascot)
            }
        }
        return null
    }

    fun hasOverlappingMascotsAtPoint(anchor: Point): Boolean {
        synchronized(mascots) {
            var count = 0
            for (mascot in mascots) {
                if (mascot.anchor == anchor) count++
                if (count > 1) return true
            }
        }
        return false
    }

    fun disposeAll() {
        synchronized(mascots) {
            val totalMascots = mascots.size
            for (i in totalMascots - 1 downTo 0) {
                mascots[i].dispose()
            }
        }
    }

    companion object {
        private val log = Logger.getLogger(this::class.java.name)
        const val TICK_INTERVAL = 40L
    }
}
