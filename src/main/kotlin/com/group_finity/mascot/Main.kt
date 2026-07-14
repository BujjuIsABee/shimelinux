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

import com.formdev.flatlaf.FlatLaf
import com.group_finity.mascot.config.Configuration
import com.group_finity.mascot.config.Entry
import com.group_finity.mascot.exception.BehaviorInstantiationException
import com.group_finity.mascot.exception.CantBeAliveException
import com.group_finity.mascot.image.ImagePairs
import com.group_finity.mascot.imagesetchooser.ImageSetChooser
import dorkbox.systemTray.Checkbox
import dorkbox.systemTray.Menu
import dorkbox.systemTray.MenuItem
import dorkbox.systemTray.SystemTray
import org.xml.sax.SAXParseException
import java.awt.Point
import java.awt.image.BufferedImage
import java.io.File
import java.util.Locale
import java.util.Properties
import java.util.ResourceBundle
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JSeparator
import javax.swing.UIManager
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.io.path.createDirectories
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    try {
        val debug = args.contains("--debug") || args.contains("-d")
        val openChooser = args.contains("--chooser") || args.contains("-c")
        val openSettings = args.contains("--settings") || args.contains("-s")

        if (!debug) {
            try {
                Main.loadResource("/conf/logging.properties").use {
                    LogManager.getLogManager().readConfiguration(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        Main.instance.run(openChooser, openSettings)
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
    private var imageSets = mutableListOf<String>()
    private val configurations = ConcurrentHashMap<String, Configuration>()
    private val childImageSets = ConcurrentHashMap<String, MutableList<String>>()

    lateinit var properties: Properties
        private set
    lateinit var languageBundle: ResourceBundle
        private set

    fun run(openChooser: Boolean, openSettings: Boolean) {
        // Set up config directory
        try {
            val resources = mutableListOf(
                "/conf/actions.xml",
                "/conf/behaviors.xml",
                "/conf/settings.properties",
                "/conf/theme/FlatDarkLaf.properties",
                "/conf/theme/FlatLightLaf.properties",
                "/img/unused/",
            )

            // Only add default mascot if the entire image directory is missing
            if (!getPath("img").exists()) {
                resources += (1..46).map { "/img/Shimeji/shime$it.png" }
            }

            for (resource in resources) {
                val destination = getPath().resolve(resource.removePrefix("/"))

                if (resource.endsWith('/')) {
                    destination.createDirectories()
                } else if (!destination.exists()) {
                    destination.createParentDirectories()
                    destination.outputStream().use { output ->
                        loadResource(resource)?.use { input ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            showError("Failed to create the config directory.", e)
            exitProcess(0)
        }

        // Load properties
        properties = Properties().apply {
            runCatching {
                getPath("conf", "settings.properties").inputStream().use {
                    load(it)
                }
            }
        }

        // Set menu scaling
        val defaultMenuScaling = System.getProperty("sun.java2d.uiScale")?.toIntOrNull() ?: 1
        val menuScaling = getProperty("MenuScaling", defaultMenuScaling)
        System.setProperty("sun.java2d.uiScale", menuScaling.toString())

        // Set theme
        try {
            FlatLaf.registerCustomDefaultsSource(getPath("conf", "theme").toFile())

            UIManager.setLookAndFeel(
                when (getProperty("Theme", "FlatDark")) {
                    "FlatDark" -> "com.formdev.flatlaf.FlatDarkLaf"
                    "FlatLight" -> "com.formdev.flatlaf.FlatLightLaf"
                    "GTK" -> "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
                    else -> "com.formdev.flatlaf.FlatDarkLaf"
                }
            )
        } catch (_: Exception) {
            log.log(Level.WARNING, "Failed to set theme.")
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName())
        }

        // Load languages
        try {
            updateConfigFile()

            val defaultLocale = Locale.getDefault().toLanguageTag()
            val locale = Locale.forLanguageTag(getProperty("Language", defaultLocale))
            languageBundle = ResourceBundle.getBundle("conf.language", locale)
        } catch (_: Exception) {
            showError("The default language file could not be loaded.")
            exit()
        }

        if (openChooser) {
            ImageSetChooser(null, true).display()
        }

        if (openSettings) {
            val settings = SettingsWindow(null, true)
            settings.isVisible = true
        }

        if (openChooser || openSettings) {
            exit()
        }

        // Get the image sets to use
        if (!getProperty("AlwaysShowShimejiChooser", false)) {
            for (set in getProperty("ActiveShimeji", "").split('/')) {
                if (set.trim().isNotEmpty()) {
                    imageSets.add(set.trim())
                }
            }
        }
        do {
            // If no image sets are selected, show the image set chooser
            if (imageSets.isEmpty()) {
                val selectedImageSets = ImageSetChooser(null, true).display()
                if (selectedImageSets != null) {
                    imageSets = selectedImageSets
                } else {
                    exit()
                }
            }

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
        } while (imageSets.isEmpty())

        // Create the tray icon
        if (!getProperty("TrayIconDisabled", false)) {
            createTrayIcon()
        }

        // Create the first mascot
        for (imageSet in imageSets) {
            val infoAlreadySeen = getProperty("InformationDismissed", "")
            val alwaysShowInfo = getProperty("AlwaysShowInformationScreen", false)
            configurations[imageSet]?.let { config ->
                if (config.containsInformationKey("SplashImage") && (alwaysShowInfo || !infoAlreadySeen.contains(imageSet))) {
                    val info = InformationWindow(imageSet, config)
                    info.isVisible = true
                    setMascotInformationDismissed(imageSet)
                    updateConfigFile()
                }
            }
            createMascot(imageSet)
        }

        manager.start()
    }

    private fun loadConfiguration(imageSet: String): Boolean {
        try {
            // Load actions
            val actionsNames = listOf(
                "actions.xml",
                "\u52D5\u4F5C.xml",
                "\u00D5\u00EF\u00F2\u00F5\u00A2\u00A3.xml",
                "\u00A6-\u00BA@.xml",
                "\u00F4\u00AB\u00EC\u00FD.xml",
                "one.xml",
                "1.xml"
            )

            var filePath = getPath("conf")
            var actionsPath = filePath.resolve("\u52D5\u4F5C.xml").takeIf { it.exists() } ?: filePath.resolve("actions.xml")

            filePath = getPath("conf", imageSet)
            actionsPath = actionsNames.map { filePath.resolve(it) }.firstOrNull { it.exists() } ?: actionsPath

            filePath = getPath("img", imageSet, "conf")
            actionsPath = actionsNames.map { filePath.resolve(it) }.firstOrNull { it.exists() } ?: actionsPath

            log.log(Level.INFO, "Reading action file ($actionsPath)")

            val actions = actionsPath.inputStream().use { DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it) }
            val configuration = Configuration()
            configuration.load(Entry(actions.documentElement), imageSet)

            // Load behaviors
            val behaviorsNames = listOf(
                "behaviors.xml",
                "\u884C\u52D5.xml",
                "\u00DE\u00ED\u00EE\u00D5\u00EF\u00F2.xml",
                "\u00AA\u00B5\u00A6-.xml",
                "\u00ECs\u00F4\u00AB.xml",
                "two.xml",
                "2.xml"
            )

            filePath = getPath("conf")
            var behaviorsPath = filePath.resolve("\u884C\u52D5.xml").takeIf { it.exists() } ?: filePath.resolve("behaviors.xml")

            filePath = getPath("conf", imageSet)
            behaviorsPath = behaviorsNames.map { filePath.resolve(it) }.firstOrNull { it.exists() } ?: behaviorsPath

            filePath = getPath("img", imageSet, "conf")
            behaviorsPath = behaviorsNames.map { filePath.resolve(it) }.firstOrNull { it.exists() } ?: behaviorsPath

            log.log(Level.INFO, "Reading behavior file ($behaviorsPath)")

            val behaviors = behaviorsPath.inputStream().use { DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it) }
            configuration.load(Entry(behaviors.documentElement), imageSet)

            // Load info
            filePath = getPath("conf")
            var infoPath = filePath.resolve("info.xml")

            filePath = getPath("conf", imageSet)
            infoPath = filePath.resolve("info.xml").takeIf { it.exists() } ?: infoPath

            filePath = getPath("img", imageSet, "conf")
            infoPath = filePath.resolve("info.xml").takeIf { it.exists() } ?: infoPath

            if (infoPath.exists()) {
                log.log(Level.INFO, "Reading information file ($infoPath)")

                val information = infoPath.inputStream().use { DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it) }
                configuration.load(Entry(information.documentElement), imageSet)
            }

            configuration.validate()

            configurations[imageSet] = configuration

            val childMascots = mutableListOf<String>()
            for (list in Entry(actions.documentElement).selectChildren("ActionList")) {
                for (node in list.selectChildren("Action")) {
                    var set = node.getAttribute("BornMascot")
                    if (set != null) {
                        if (!childMascots.contains(set)) {
                            childMascots.add(set)
                        }
                        if (!configurations.containsKey(set)) {
                            loadConfiguration(set)
                        }
                    }
                    set = node.getAttribute("TransformMascot")
                    if (set != null) {
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
            showError("FailedLoadConfigErrorMessage".localize(), e)
        }

        return false
    }

    private fun createTrayIcon() {
        log.log(Level.INFO, "Creating the tray icon")

        try {
            val icon = SystemTray.get()
            loadResource("/img/icon.png").use { icon.setImage(it) }
            icon.setStatus("ShimeLinux")

            val callShimejiMenu = MenuItem("CallShimeji".localize()) {
                createMascot()
            }

            val followCursorMenu = MenuItem("FollowCursor".localize()) {
                manager.setBehaviorAll("ChaseMouse")
            }

            val reduceToOneMenu = MenuItem("ReduceToOne".localize()) {
                manager.remainOne()
            }

            val restoreWindowsMenu = MenuItem("RestoreWindows".localize()) {
                NativeFactory.instance.environment.restoreIE()
            }

            val breedingMenu = Checkbox("BreedingCloning".localize()) {
                toggleBooleanSetting("Breeding", true)
                updateConfigFile()
            }
            breedingMenu.checked = getProperty("Breeding", true)

            val transientMenu = Checkbox("BreedingTransient".localize()) {
                toggleBooleanSetting("Transients", true)
                updateConfigFile()
            }
            transientMenu.checked = getProperty("Transients", true)

            val transformationMenu = Checkbox("Transformation".localize()) {
                toggleBooleanSetting("Transformation", true)
                updateConfigFile()
            }
            transformationMenu.checked = getProperty("Transformation", true)

            val throwingMenu = Checkbox("ThrowingWindows".localize()) {
                toggleBooleanSetting("Throwing", true)
                updateConfigFile()
            }
            throwingMenu.checked = getProperty("Throwing", true)

            val soundsMenu = Checkbox("SoundEffects".localize()) {
                toggleBooleanSetting("Sounds", true)
                updateConfigFile()
            }
            soundsMenu.checked = getProperty("Sounds", true)

            val multiscreenMenu = Checkbox("Multiscreen".localize()) {
                toggleBooleanSetting("Multiscreen", true)
                updateConfigFile()
            }
            multiscreenMenu.checked = getProperty("Multiscreen", true)

            val allowedBehaviorsSubmenu = Menu("AllowedBehaviors".localize())
            allowedBehaviorsSubmenu.add(breedingMenu)
            allowedBehaviorsSubmenu.add(transientMenu)
            allowedBehaviorsSubmenu.add(transformationMenu)
            allowedBehaviorsSubmenu.add(throwingMenu)
            allowedBehaviorsSubmenu.add(soundsMenu)
            allowedBehaviorsSubmenu.add(multiscreenMenu)

            val chooseShimejiMenu = MenuItem("ChooseShimeji".localize()) {
                if (!manager.isPaused) {
                    manager.togglePauseAll()
                }

                val chooser = ImageSetChooser(null, true)
                setActiveImageSets(chooser.display())

                if (manager.isPaused) {
                    manager.togglePauseAll()
                }
            }

            val settingsMenu = MenuItem("Settings".localize()) {
                if (!manager.isPaused) {
                    manager.togglePauseAll()
                }

                val settings = SettingsWindow(null, true)
                settings.isVisible = true

                if (settings.isRestartRequired) {
                    val jarPath = this::class.java.protectionDomain.codeSource.location.path
                    val restartProcess = ProcessBuilder("java", "-jar", jarPath)
                    restartProcess.directory(File(System.getProperty("user.dir")))
                    restartProcess.start()
                    exit()
                }
                if (settings.isEnvironmentReloadRequired) {
                    NativeFactory.instance.environment.dispose()
                    NativeFactory.resetInstance()
                }
                if (settings.isEnvironmentReloadRequired || settings.isImageReloadRequired) {
                    val isExitOnLastRemoved = manager.isExitOnLastRemoved
                    manager.isExitOnLastRemoved = false
                    manager.disposeAll()

                    ImagePairs.clear()
                    configurations.clear()

                    for (imageSet in imageSets) {
                        loadConfiguration(imageSet)
                    }
                    for (imageSet in imageSets) {
                        createMascot(imageSet)
                    }

                    manager.isExitOnLastRemoved = isExitOnLastRemoved
                }
                if (settings.isInteractiveWindowReloadRequired) {
                    NativeFactory.instance.environment.refreshCache()
                }

                if (manager.isPaused) {
                    manager.togglePauseAll()
                }
            }

            val americanEnglishMenu = MenuItem("English (US)") {
                updateLanguage("en-US")
                updateConfigFile()
            }

            val britishEnglishMenu = MenuItem("English (UK)") {
                updateLanguage("en-UK")
                updateConfigFile()
            }

            val arabicMenu = MenuItem("\u0639\u0631\u0628\u064A") {
                updateLanguage("ar-SA")
                updateConfigFile()
            }

            val catalanMenu = MenuItem("Catal\u00E0") {
                updateLanguage("ca-ES")
                updateConfigFile()
            }

            val germanMenu = MenuItem("Deutsch") {
                updateLanguage("de-DE")
                updateConfigFile()
            }

            val spanishMenu = MenuItem("Espa\u00F1ol") {
                updateLanguage("es-ES")
                updateConfigFile()
            }

            val frenchMenu = MenuItem("Fran\u00E7ais") {
                updateLanguage("fr-FR")
                updateConfigFile()
            }

            val croatianMenu = MenuItem("Hrvatski") {
                updateLanguage("hr-HR")
                updateConfigFile()
            }

            val italianMenu = MenuItem("Italiano") {
                updateLanguage("it-IT")
                updateConfigFile()
            }

            val dutchMenu = MenuItem("Nederlands") {
                updateLanguage("nl-NL")
                updateConfigFile()
            }

            val polishMenu = MenuItem("Polski") {
                updateLanguage("pl-PL")
                updateConfigFile()
            }

            val portugueseMenu = MenuItem("Portugu\u00eas") {
                updateLanguage("pt-PT")
                updateConfigFile()
            }

            val brazilianPortugueseMenu = MenuItem("Portugu\u00eas Brasileiro") {
                updateLanguage("pt-BR")
                updateConfigFile()
            }

            val russianMenu = MenuItem("\u0440\u0443\u0301\u0441\u0441\u043a\u0438\u0439 \u044f\u0437\u044b\u0301\u043a") {
                updateLanguage("ru-RU")
                updateConfigFile()
            }

            val romanianMenu = MenuItem("Rom\u00e2n\u0103") {
                updateLanguage("ro-RO")
                updateConfigFile()
            }

            val serbianMenu = MenuItem("Srpski") {
                updateLanguage("sr-RS")
                updateConfigFile()
            }

            val finnishMenu = MenuItem("Suomi") {
                updateLanguage("fi-FI")
                updateConfigFile()
            }

            val vietnameseMenu = MenuItem("ti\u1ebfng Vi\u1ec7t") {
                updateLanguage("vi-VN")
                updateConfigFile()
            }

            val chineseMenu = MenuItem("\u7b80\u4f53\u4e2d\u6587") {
                updateLanguage("zh-CN")
                updateConfigFile()
            }

            val chineseTraditionalMenu = MenuItem("\u7E41\u9AD4\u4E2D\u6587") {
                updateLanguage("zh-TW")
                updateConfigFile()
            }

            val koreanMenu = MenuItem("\ud55c\uad6d\uc5b4") {
                updateLanguage("ko-KR")
                updateConfigFile()
            }

            val japaneseMenu = MenuItem("\u65E5\u672C\u8A9E") {
                updateLanguage("ja-JP")
                updateConfigFile()
            }

            val languageSubmenu = Menu("Language".localize())
            languageSubmenu.add(americanEnglishMenu)
            languageSubmenu.add(britishEnglishMenu)
            languageSubmenu.add(arabicMenu)
            languageSubmenu.add(catalanMenu)
            languageSubmenu.add(germanMenu)
            languageSubmenu.add(spanishMenu)
            languageSubmenu.add(frenchMenu)
            languageSubmenu.add(croatianMenu)
            languageSubmenu.add(italianMenu)
            languageSubmenu.add(dutchMenu)
            languageSubmenu.add(polishMenu)
            languageSubmenu.add(portugueseMenu)
            languageSubmenu.add(brazilianPortugueseMenu)
            languageSubmenu.add(russianMenu)
            languageSubmenu.add(romanianMenu)
            languageSubmenu.add(serbianMenu)
            languageSubmenu.add(finnishMenu)
            languageSubmenu.add(vietnameseMenu)
            languageSubmenu.add(chineseMenu)
            languageSubmenu.add(chineseTraditionalMenu)
            languageSubmenu.add(koreanMenu)
            languageSubmenu.add(japaneseMenu)

            var pauseAllMenu: MenuItem? = null
            pauseAllMenu = MenuItem("PauseAnimations".localize()) {
                pauseAllMenu?.let { pauseMenuClicked(it) }
            }

            val dismissAllMenu = MenuItem("DismissAll".localize()) {
                exit()
            }

            icon.menu.add(callShimejiMenu)
            icon.menu.add(followCursorMenu)
            icon.menu.add(reduceToOneMenu)
            icon.menu.add(restoreWindowsMenu)
            icon.menu.add(JSeparator())
            icon.menu.add(allowedBehaviorsSubmenu)
            icon.menu.add(chooseShimejiMenu)
            icon.menu.add(settingsMenu)
            icon.menu.add(languageSubmenu)
            icon.menu.add(JSeparator())
            icon.menu.add(pauseAllMenu)
            icon.menu.add(dismissAllMenu)
        } catch (_: Exception) {
            log.log(Level.SEVERE, "Failed to create tray icon")
            showError("FailedDisplaySystemTrayErrorMessage".localize())

            setProperty("TrayIconDisabled", "true")
            updateConfigFile()
        }
    }

    private fun pauseMenuClicked(pauseMenuItem: MenuItem) {
        manager.togglePauseAll()

        // Update pause menu item text
        pauseMenuItem.text = if (manager.isPaused) {
            "ResumeAnimations".localize()
        } else {
            "PauseAnimations".localize()
        }
    }

    private fun createMascot() {
        val length = imageSets.size
        val random = (length * Math.random()).toInt()
        createMascot(imageSets[random])
    }

    fun createMascot(imageSet: String) {
        log.log(Level.INFO, "Creating a mascot ($imageSet)")

        val mascot = Mascot(imageSet)

        // Create the mascot outside the screen bounds so its position gets reset
        mascot.anchor = Point(-4000, -4000)

        // Randomize the initial direction
        mascot.isLookRight = Math.random() < 0.5

        try {
            mascot.behavior = checkNotNull(getConfiguration(imageSet)).buildNextBehavior(null, mascot)
            manager.add(mascot)
        } catch (e: BehaviorInstantiationException) {
            log.log(Level.SEVERE, "Failed to initialize the first action", e)
            showError("FailedInitializeFirstActionErrorMessage".localize(), e)
            mascot.dispose()
        } catch (e: CantBeAliveException) {
            log.log(Level.SEVERE, "Fatal Error", e)
            showError("FailedInitializeFirstActionErrorMessage".localize(), e)
            mascot.dispose()
        } catch (e: Exception) {
            log.log(Level.SEVERE, "Could not be started ($imageSet)", e)
            showError("CouldNotCreateShimejiErrorMessage".localize() + " ($imageSet)", e)
            mascot.dispose()
        }
    }

    private fun updateLanguage(language: String) {
        if (getProperty("Language", "en-US") != language) {
            setProperty("Language", language)
            refreshLanguage()
        }
    }

    private fun refreshLanguage() {
        try {
            // Refresh the language bundle
            val systemLocale = Locale.getDefault().toLanguageTag()
            val locale = Locale.forLanguageTag(getProperty("Language", systemLocale))
            languageBundle = ResourceBundle.getBundle("conf.language", locale)

            // Refresh the mascots
            val isExitOnLastRemoved = manager.isExitOnLastRemoved
            manager.isExitOnLastRemoved = false
            manager.disposeAll()

            for (imageSet in imageSets) {
                loadConfiguration(imageSet)
            }

            for (imageSet in imageSets) {
                createMascot(imageSet)
            }

            manager.isExitOnLastRemoved = isExitOnLastRemoved

            // Refresh the tray icon
            if (!getProperty("TrayIconDisabled", false)) {
                val icon = SystemTray.get()
                for (entry in icon.menu.entries) {
                    icon.menu.remove(entry)
                }
                createTrayIcon()
            }
        } catch (_: Exception) {
            showError("Failed to set the new language.")
            exit()
        }
    }

    fun setMascotBehaviorEnabled(name: String, mascot: Mascot, enabled: Boolean) {
        val list = mutableListOf<String>()
        val data = getProperty("DisabledBehaviours.${mascot.imageSet}", "").split('/')

        if (data.isNotEmpty() && data[0] != "") {
            list.addAll(data)
        }

        if (list.contains(name) && enabled) {
            list.remove(name)
        } else if (!list.contains(name) && !enabled) {
            list.add(name)
        }

        if (list.isNotEmpty()) {
            setProperty(
                "DisabledBehaviours.${mascot.imageSet}",
                list.toString()
                    .replace("[", "")
                    .replace("]", "")
                    .replace(", ", "/")
            )
        } else {
            properties.remove("DisabledBehaviours.${mascot.imageSet}")
        }

        updateConfigFile()
    }

    @Suppress("SameParameterValue")
    private fun toggleBooleanSetting(propertyName: String, defaultValue: Boolean) {
        if (getProperty(propertyName, defaultValue)) {
            setProperty(propertyName, "false")
        } else {
            setProperty(propertyName, "true")
        }
    }

    private fun setMascotInformationDismissed(imageSet: String) {
        val list = mutableListOf<String>()
        val data = getProperty("InformationDismissed", "").split('/')

        if (data.isNotEmpty() && data[0].isNotEmpty()) {
            list.addAll(data.toList())
        }
        if (!list.contains(imageSet)) {
            list.add(imageSet)
        }

        setProperty(
            "InformationDismissed",
            list.toString()
                .replace("[", "")
                .replace("]", "")
                .replace(", ", "/")
        )
    }

    private fun updateConfigFile() {
        try {
            getPath("conf", "settings.properties").outputStream().use {
                properties.store(it, "Configuration Options")
            }
        } catch (_: Exception) {
        }
    }

    private fun setActiveImageSets(newImageSets: MutableList<String>?) {
        if (newImageSets == null) return

        val toRemove = imageSets.toMutableList()
        toRemove.removeAll(newImageSets)

        val toAdd = mutableListOf<String>()
        val toRetain = mutableListOf<String>()

        for (set in newImageSets) {
            if (!imageSets.contains(set)) {
                toAdd.add(set)
            }
            if (!toRetain.contains(set)) {
                toRetain.add(set)
            }
            populateArrayListWithChildSets(set, toRetain)
        }

        val isExitOnLastRemoved = manager.isExitOnLastRemoved
        manager.isExitOnLastRemoved = false

        for (removed in toRemove) {
            removeLoadedImageSet(removed, toRetain)
        }

        for (added in toAdd) {
            addImageSet(added)
        }

        manager.isExitOnLastRemoved = isExitOnLastRemoved
    }

    private fun populateArrayListWithChildSets(imageSet: String, childList: MutableList<String>) {
        childImageSets[imageSet]?.let {
            for (set in it) {
                if (!childList.contains(set)) {
                    populateArrayListWithChildSets(set, childList)
                    childList.add(set)
                }
            }
        }
    }

    private fun removeLoadedImageSet(imageSet: String, setsToIgnore: MutableList<String>) {
        childImageSets[imageSet]?.let {
            for (set in it) {
                if (!setsToIgnore.contains(set)) {
                    setsToIgnore.add(set)
                    imageSets.remove(imageSet)
                    manager.remainNone(imageSet)
                    configurations.remove(imageSet)
                    ImagePairs.removeAll(imageSet)
                    removeLoadedImageSet(set, setsToIgnore)
                }
            }
        }

        if (!setsToIgnore.contains(imageSet)) {
            imageSets.remove(imageSet)
            manager.remainNone(imageSet)
            configurations.remove(imageSet)
            ImagePairs.removeAll(imageSet)
        }
    }

    private fun addImageSet(imageSet: String) {
        if (configurations.containsKey(imageSet)) {
            imageSets.add(imageSet)
            createMascot(imageSet)
        } else {
            if (loadConfiguration(imageSet)) {
                imageSets.add(imageSet)

                val infoAlreadySeen = getProperty("InformationDismissed", "")
                val alwaysShowInfo = getProperty("AlwaysShowInformationScreen", false)
                configurations[imageSet]?.let { config ->
                    if (config.containsInformationKey("SplashImage") && (alwaysShowInfo || !infoAlreadySeen.contains(imageSet))) {
                        val info = InformationWindow(imageSet, config)
                        info.isVisible = true
                        setMascotInformationDismissed(imageSet)
                        updateConfigFile()
                    }
                }
                createMascot(imageSet)
            } else {
                // Failed to load
                configurations.remove(imageSet)
            }
        }
    }

    fun getConfiguration(imageSet: String) = configurations[imageSet]

    fun exit() {
        manager.disposeAll()
        manager.stop()
        exitProcess(0)
    }

    companion object {
        private val log = Logger.getLogger(this::class.java.name)

        @JvmStatic
        val instance = Main()

        @JvmStatic
        val frame: JFrame by lazy {
            JFrame().apply {
                iconImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
            }
        }

        @JvmStatic
        fun showError(message: String) {
            JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE)
        }

        @JvmStatic
        fun showError(message: String, exception: Throwable) {
            val message = message + if (exception is SAXParseException) {
                "\nLine ${exception.lineNumber}: ${exception.message}"
            } else {
                "\n${exception.message}"
            }

            showError(message + "\n${"SeeLogForDetails".localize()}")
        }
    }
}
