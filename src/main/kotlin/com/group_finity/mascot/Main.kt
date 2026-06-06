/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot

import com.group_finity.mascot.config.Configuration
import com.group_finity.mascot.config.Entry
import com.group_finity.mascot.exception.BehaviorInstantiationException
import com.group_finity.mascot.exception.CantBeAliveException
import org.xml.sax.SAXParseException
import java.awt.Point
import java.util.Locale
import java.util.Properties
import java.util.ResourceBundle
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.JOptionPane
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.system.exitProcess

fun main() {
    try {
        Main.instance.run()
    } catch (_: OutOfMemoryError) {
        Main.showError(
            "Out of Memory. There are probably too many\n" +
            "Shimeji mascots for your computer to handle.\n" +
            "Select fewer image sets or move some to the\n" +
            "img/unused folder and try again."
        )
        exitProcess(0)
    }
}

class Main {
    private val manager = Manager()
    private val imageSets = ArrayList<String>()
    private val configurations = ConcurrentHashMap<String, Configuration>()
    private val childImageSets = ConcurrentHashMap<String, ArrayList<String>>()

    lateinit var properties: Properties
        private set
    lateinit var languageBundle: ResourceBundle
        private set

    fun run() {
        // Load properties
        try {
            val input = this::class.java.getResourceAsStream("/conf/settings.properties")
            properties = Properties()
            properties.load(input)
        } catch (_: Exception) {
        }

        // Load languages
        try {
            val systemLocale = Locale.getDefault().toLanguageTag()
            val locale = Locale.forLanguageTag(properties.getProperty("Language", systemLocale))
            languageBundle = ResourceBundle.getBundle("conf.language", locale)
        } catch (_: Exception) {
            showError("The default language file could not be loaded.")
            exit()
        }

        // TODO: add image set chooser
        imageSets.add("Shimeji")

        // Load mascots
        var index = 0
        while (index < imageSets.size) {
            if (!loadConfiguration(imageSets[index])) {
                // Failed to load
                configurations.remove(imageSets[index])
                imageSets.remove(imageSets[index])
                index--
            }
            index++
        }

        // TODO: add tray icon

        // Create the first mascot
        for (imageSet in imageSets) {
            // TODO: add information
            createMascot(imageSet)
        }

        manager.start()
    }

    private fun loadConfiguration(imageSet: String): Boolean {
        try {
            var filePath = Path("/conf")
            var actionsPath = filePath.resolve("actions.xml")
            if (filePath.resolve("\u52D5\u4F5C.xml").exists()) {
                actionsPath = filePath.resolve("\u52D5\u4F5C.xml")
            }

            filePath = Path("/conf/$imageSet")
            if (filePath.resolve("actions.xml").exists()) {
                actionsPath = filePath.resolve("actions.xml")
            } else if (filePath.resolve("\u52D5\u4F5C.xml").exists()) {
                actionsPath = filePath.resolve("\u52D5\u4F5C.xml")
            } else if (filePath.resolve("\u00D5\u00EF\u00F2\u00F5\u00A2\u00A3.xml").exists()) {
                actionsPath = filePath.resolve("\u00D5\u00EF\u00F2\u00F5\u00A2\u00A3.xml")
            } else if (filePath.resolve("\u00A6-\u00BA@.xml").exists()) {
                actionsPath = filePath.resolve("\u00A6-\u00BA@.xml")
            } else if (filePath.resolve("\u00F4\u00AB\u00EC\u00FD.xml").exists()) {
                actionsPath = filePath.resolve("\u00F4\u00AB\u00EC\u00FD.xml")
            } else if (filePath.resolve("one.xml").exists()) {
                actionsPath = filePath.resolve("one.xml")
            } else if (filePath.resolve("1.xml").exists()) {
                actionsPath = filePath.resolve("1.xml")
            }

            filePath = Path("/img/$imageSet/conf")
            if (filePath.resolve("actions.xml").exists()) {
                actionsPath = filePath.resolve("actions.xml")
            } else if (filePath.resolve("\u52D5\u4F5C.xml").exists()) {
                actionsPath = filePath.resolve("\u52D5\u4F5C.xml")
            } else if (filePath.resolve("\u00D5\u00EF\u00F2\u00F5\u00A2\u00A3.xml").exists()) {
                actionsPath = filePath.resolve("\u00D5\u00EF\u00F2\u00F5\u00A2\u00A3.xml")
            } else if (filePath.resolve("\u00A6-\u00BA@.xml").exists()) {
                actionsPath = filePath.resolve("\u00A6-\u00BA@.xml")
            } else if (filePath.resolve("\u00F4\u00AB\u00EC\u00FD.xml").exists()) {
                actionsPath = filePath.resolve("\u00F4\u00AB\u00EC\u00FD.xml")
            } else if (filePath.resolve("one.xml").exists()) {
                actionsPath = filePath.resolve("one.xml")
            } else if (filePath.resolve("1.xml").exists()) {
                actionsPath = filePath.resolve("1.xml")
            }

            log.log(Level.INFO, "Reading action file ($actionsPath)")

            val actions = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this::class.java.getResourceAsStream(actionsPath.toString()))

            val configuration = Configuration()

            configuration.load(Entry(actions.documentElement), imageSet)

            filePath = Path("/conf")
            var behaviorsPath = filePath.resolve("behaviors.xml")
            if (filePath.resolve("\u884C\u52D5.xml").exists()) {
                behaviorsPath = filePath.resolve("\u884C\u52D5.xml")
            }

            filePath = Path("/conf/$imageSet")
            if (filePath.resolve("behaviors.xml").exists()) {
                behaviorsPath = filePath.resolve("behaviors.xml")
            } else if (filePath.resolve("behavior.xml").exists()) {
                behaviorsPath = filePath.resolve("behavior.xml")
            } else if (filePath.resolve("\u884C\u52D5.xml").exists()) {
                behaviorsPath = filePath.resolve("\u884C\u52D5.xml")
            } else if (filePath.resolve("\u00DE\u00ED\u00EE\u00D5\u00EF\u00F2.xml").exists()) {
                behaviorsPath = filePath.resolve("\u00DE\u00ED\u00EE\u00D5\u00EF\u00F2.xml")
            } else if (filePath.resolve("\u00AA\u00B5\u00A6-.xml").exists()) {
                behaviorsPath = filePath.resolve("\u00AA\u00B5\u00A6-.xml")
            } else if (filePath.resolve("\u00ECs\u00F4\u00AB.xml").exists()) {
                behaviorsPath = filePath.resolve("\u00ECs\u00F4\u00AB.xml")
            } else if (filePath.resolve("two.xml").exists()) {
                behaviorsPath = filePath.resolve("two.xml")
            } else if (filePath.resolve("2.xml").exists()) {
                behaviorsPath = filePath.resolve("2.xml")
            }

            filePath = Path("/img/$imageSet/conf")
            if (filePath.resolve("behaviors.xml").exists()) {
                behaviorsPath = filePath.resolve("behaviors.xml")
            } else if (filePath.resolve("behavior.xml").exists()) {
                behaviorsPath = filePath.resolve("behavior.xml")
            } else if (filePath.resolve("\u884C\u52D5.xml").exists()) {
                behaviorsPath = filePath.resolve("\u884C\u52D5.xml")
            } else if (filePath.resolve("\u00DE\u00ED\u00EE\u00D5\u00EF\u00F2.xml").exists()) {
                behaviorsPath = filePath.resolve("\u00DE\u00ED\u00EE\u00D5\u00EF\u00F2.xml")
            } else if (filePath.resolve("\u00AA\u00B5\u00A6-.xml").exists()) {
                behaviorsPath = filePath.resolve("\u00AA\u00B5\u00A6-.xml")
            } else if (filePath.resolve("\u00ECs\u00F4\u00AB.xml").exists()) {
                behaviorsPath = filePath.resolve("\u00ECs\u00F4\u00AB.xml")
            } else if (filePath.resolve("two.xml").exists()) {
                behaviorsPath = filePath.resolve("two.xml")
            } else if (filePath.resolve("2.xml").exists()) {
                behaviorsPath = filePath.resolve("2.xml")
            }

            log.log(Level.INFO, "Reading behavior file ($behaviorsPath)")

            val behaviors = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this::class.java.getResourceAsStream(behaviorsPath.toString()))

            configuration.load(Entry(behaviors.documentElement), imageSet)

            // TODO load info

            configuration.validate()

            configurations[imageSet] = configuration

            val childMascots = ArrayList<String>()

            for (list in Entry(actions.documentElement).selectChildren("ActionList")) {
                for (node in list.selectChildren("Action")) {
                    if (node.attributes.containsKey("BornMascot")) {
                        val set = node.getAttribute("BornMascot")!!
                        if (!childMascots.contains(set)) {
                            childMascots.add(set)
                        }
                        if (!configurations.containsKey(set)) {
                            loadConfiguration(set)
                        }
                    }
                    if (node.attributes.containsKey("TransformMascot")) {
                        val set = node.getAttribute("TransformMascot")!!
                        if (!childMascots.contains(set)) {
                            childMascots.add(set)
                        }
                        if (!configurations.containsKey(set)) {
                            loadConfiguration(set)
                        }
                    }
                }
            }

            childImageSets[imageSet] = childMascots

            return true
        } catch (e: Exception) {
            log.log(Level.SEVERE, "Failed to load configuration files", e)
            showError(languageBundle.getString("FailedLoadConfigErrorMessage"), e)
        }

        return false
    }

    private fun createMascot() {
        val length = imageSets.size
        val random = (length * Math.random()).toInt()
        createMascot(imageSets[random])
    }

    fun createMascot(imageSet: String) {
        log.log(Level.INFO, "Creating a mascot ($imageSet)")

        val mascot = Mascot(imageSet)
        mascot.anchor = Point(-4000, -4000)
        mascot.isLookRight = Math.random() < 0.5

        try {
            mascot.behavior = checkNotNull(getConfiguration(imageSet)).buildNextBehavior(null, mascot)
            manager.add(mascot)
        } catch (e: BehaviorInstantiationException) {
            log.log(Level.SEVERE, "Failed to initialize the first action", e)
            showError(languageBundle.getString("FailedInitialiseFirstActionErrorMessage"), e)
            mascot.dispose()
        } catch (e: CantBeAliveException) {
            log.log(Level.SEVERE, "Fatal Error", e)
            showError(languageBundle.getString("FailedInitialiseFirstActionErrorMessage"), e)
            mascot.dispose()
        } catch (e: Exception) {
            log.log(Level.SEVERE, "Could not be started ($imageSet)", e)
            showError(languageBundle.getString("CouldNotCreateShimejiErrorMessage") + " ($imageSet)", e)
            mascot.dispose()
        }
    }

    fun getConfiguration(imageSet: String): Configuration? {
        return configurations[imageSet]
    }

    fun exit() {
        exitProcess(0)
    }

    companion object {
        @JvmStatic
        val instance = Main()

        private val log = Logger.getLogger(this::class.java.name)

        @JvmStatic
        fun showError(message: String) {
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE)
        }

        @JvmStatic
        fun showError(message: String, exception: Throwable) {
            val m = message + if (exception is SAXParseException) {
                "\nLine ${exception.lineNumber}: ${exception.message}"
            } else {
                "\n${exception.message}"
            }

            showError(m)
        }
    }
}
