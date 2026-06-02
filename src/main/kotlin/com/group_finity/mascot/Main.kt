/*
 * ShimeLinux is an unofficial Linux version/Kotlin rewrite of Shimeji-ee by Kilkakon (https://kilkakon.com/shimeji)
 * View the full license here: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 */

package com.group_finity.mascot

import org.xml.sax.SAXParseException
import java.util.Locale
import java.util.Properties
import java.util.ResourceBundle
import javax.swing.JOptionPane
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
    lateinit var properties: Properties
        private set
    lateinit var languageBundle: ResourceBundle
        private set

    fun run() {
        // Load properties
        try {
            val input = this::class.java.classLoader.getResourceAsStream("/conf/settings.properties")
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
    }

    fun exit() {
        exitProcess(0)
    }

    companion object {
        @JvmStatic
        val instance = Main()

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
