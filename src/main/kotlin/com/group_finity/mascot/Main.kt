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
import com.group_finity.mascot.image.ImagePairs
import com.group_finity.mascot.imagesetchooser.ImageSetChooser
import dorkbox.systemTray.Checkbox
import dorkbox.systemTray.Menu
import dorkbox.systemTray.MenuItem
import dorkbox.systemTray.SystemTray
import org.xml.sax.SAXParseException
import java.awt.Point
import java.awt.Toolkit
import java.io.File
import java.nio.file.Path
import java.util.Locale
import java.util.Properties
import java.util.ResourceBundle
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger
import javax.swing.JOptionPane
import javax.swing.JSeparator
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.outputStream
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    try {
        Main.createConfigDirectory()

        Main.instance.init()

        if (!args.contains("DEBUG")) {
            Main.configureLogging()
        }

        SwingUtilities.invokeLater {
            Main.instance.run()
        }
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

    fun init() {
        runCatching {
            // Load properties
            properties = Properties()
            getPath("conf", "settings.properties").inputStream().use { properties.load(it) }

            // Set menu scaling
            if (properties.containsKey("MenuDPI")) {
                val menuScaling = properties.getProperty("MenuDPI", "96").toInt() / 96.0f
                System.setProperty("sun.java2d.uiScale", menuScaling.toString())
            } else {
                val dpi = Toolkit.getDefaultToolkit().screenResolution.coerceAtLeast(96)
                properties.setProperty("MenuDPI", dpi.toString())
                updateConfigFile()

                if (dpi != 96) {
                    // Restart
                    val jarPath = this::class.java.protectionDomain.codeSource.location.path
                    val restartProcess = ProcessBuilder("java", "-jar", jarPath)
                    restartProcess.directory(File(System.getProperty("user.dir")))
                    restartProcess.start()
                    exitProcess(0)
                }
            }
        }

        // Set theme
        try {
            UIManager.setLookAndFeel(when (properties.getProperty("Theme", "GTK")) {
                "GTK" -> "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
                "Nimbus" -> "javax.swing.plaf.nimbus.NimbusLookAndFeel"
                "Metal" -> "javax.swing.plaf.metal.MetalLookAndFeel"
                else -> "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
            })
        } catch (_: Exception) {
            log.log(Level.WARNING, "Failed to set theme.")
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName())
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

    fun run() {
        // Get the image sets to use
        if (!properties.getProperty("AlwaysShowShimejiChooser", "false").toBoolean()) {
            for (set in properties.getProperty("ActiveShimeji", "").split('/')) {
                if (set.trim().isNotEmpty()) {
                    imageSets.add(set.trim())
                }
            }
        }
        do {
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
        createTrayIcon()

        // Create the first mascot
        for (imageSet in imageSets) {
            val infoAlreadySeen = properties.getProperty("InformationDismissed", "")
            val alwaysShowInfo = properties.getProperty("AlwaysShowInformationScreen", "false").toBoolean()
            configurations[imageSet]?.let { config ->
                if (config.containsInformationKey("SplashImage") && (alwaysShowInfo || !infoAlreadySeen.contains(imageSet))
                ) {
                    val info = InformationWindow(imageSet, config)
                    info.display()
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
            var filePath = getPath("conf")
            var actionsPath = filePath.resolve("actions.xml")
            if (filePath.resolve("\u52D5\u4F5C.xml").exists()) {
                actionsPath = filePath.resolve("\u52D5\u4F5C.xml")
            }

            filePath = getPath("conf", imageSet)
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

            filePath = getPath("img", imageSet, "conf")
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

            val actions = actionsPath.inputStream().use {
                DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it)
            }

            val configuration = Configuration()

            configuration.load(Entry(actions.documentElement), imageSet)

            filePath = getPath("conf")
            var behaviorsPath = filePath.resolve("behaviors.xml")
            if (filePath.resolve("\u884C\u52D5.xml").exists()) {
                behaviorsPath = filePath.resolve("\u884C\u52D5.xml")
            }

            filePath = getPath("conf", imageSet)
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

            filePath = getPath("img", imageSet, "conf")
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

            val behaviors = behaviorsPath.inputStream().use {
                DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it)
            }

            configuration.load(Entry(behaviors.documentElement), imageSet)

            filePath = getPath("conf")
            var infoPath = filePath.resolve("info.xml")

            filePath = getPath("conf", imageSet)
            if (filePath.resolve("info.xml").exists()) {
                infoPath = filePath.resolve("info.xml")
            }

            filePath = getPath("img", imageSet, "conf")
            if (filePath.resolve("info.xml").exists()) {
                infoPath = filePath.resolve("info.xml")
            }

            if (infoPath.exists()) {
                log.log(Level.INFO, "Reading information file ($infoPath)")

                val information = infoPath.inputStream().use {
                    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it)
                }

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
            showError(languageBundle.getString("FailedLoadConfigErrorMessage"), e)
        }

        return false
    }

    private fun createTrayIcon() {
        log.log(Level.INFO, "Creating the tray icon")

        val icon = SystemTray.get()
        if (icon != null) {
            this::class.java.getResourceAsStream("/img/icon.png").use {
                icon.setImage(it)
            }
            icon.setStatus("ShimeLinux")
        } else {
            log.log(Level.SEVERE, "Failed to create tray icon")
            showError(languageBundle.getString("FailedDisplaySystemTrayErrorMessage"))
        }

        val callShimejiMenu = MenuItem(languageBundle.getString("CallShimeji")) {
            createMascot()
        }

        val followCursorMenu = MenuItem(languageBundle.getString("FollowCursor")) {
            manager.setBehaviorAll("ChaseMouse")
        }

        val reduceToOneMenu = MenuItem(languageBundle.getString("ReduceToOne")) {
            manager.remainOne()
        }

        val restoreWindowsMenu = MenuItem(languageBundle.getString("RestoreWindows")) {
            NativeFactory.instance.getEnvironment().restoreIE()
        }

        //region Allowed behaviors submenu
        val breedingMenu = Checkbox(languageBundle.getString("BreedingCloning")) {
            toggleBooleanSetting("Breeding", true)
            updateConfigFile()
        }
        breedingMenu.checked = properties.getProperty("Breeding", "true").toBoolean()

        val transientMenu = Checkbox(languageBundle.getString("BreedingTransient")) {
            toggleBooleanSetting("Transients", true)
            updateConfigFile()
        }
        transientMenu.checked = properties.getProperty("Transients", "true").toBoolean()

        val transformationMenu = Checkbox(languageBundle.getString("Transformation")) {
            toggleBooleanSetting("Transformation", true)
            updateConfigFile()
        }
        transformationMenu.checked = properties.getProperty("Transformation", "true").toBoolean()

        val throwingMenu = Checkbox(languageBundle.getString("ThrowingWindows")) {
            toggleBooleanSetting("Throwing", true)
            updateConfigFile()
        }
        throwingMenu.checked = properties.getProperty("Throwing", "true").toBoolean()

        val soundsMenu = Checkbox(languageBundle.getString("SoundEffects")) {
            toggleBooleanSetting("Sounds", true)
            updateConfigFile()
        }
        soundsMenu.checked = properties.getProperty("Sounds", "true").toBoolean()

        val multiscreenMenu = Checkbox(languageBundle.getString("Multiscreen")) {
            toggleBooleanSetting("Multiscreen", true)
            updateConfigFile()
        }
        multiscreenMenu.checked = properties.getProperty("Multiscreen", "true").toBoolean()
        //endregion

        val allowedBehaviorsSubmenu = Menu(languageBundle.getString("AllowedBehaviours"))
        allowedBehaviorsSubmenu.add(breedingMenu)
        allowedBehaviorsSubmenu.add(transientMenu)
        allowedBehaviorsSubmenu.add(transformationMenu)
        allowedBehaviorsSubmenu.add(throwingMenu)
        allowedBehaviorsSubmenu.add(soundsMenu)
        allowedBehaviorsSubmenu.add(multiscreenMenu)

        val chooseShimejiMenu = MenuItem(languageBundle.getString("ChooseShimeji")) {
            val chooser = ImageSetChooser(null, true)
            setActiveImageSets(chooser.display())
        }

        val settingsMenu = MenuItem(languageBundle.getString("Settings")) {
            val settings = SettingsWindow(null, true)
            settings.display()

            if (settings.isEnvironmentReloadRequired) {
                NativeFactory.instance.getEnvironment().dispose()
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
                NativeFactory.instance.getEnvironment().refreshCache()
            }
        }

        //region Language submenu
        val englishMenu = MenuItem("English") {
            updateLanguage("en-GB")
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
        //endregion

        val languageSubmenu = Menu(languageBundle.getString("Language"))
        languageSubmenu.add(englishMenu)
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

        val pauseAllMenu = MenuItem(if (manager.isPaused) {
            languageBundle.getString("ResumeAnimations")
        } else {
            languageBundle.getString("PauseAnimations")
        }) {
            manager.togglePauseAll()
        }

        val dismissAllMenu = MenuItem(languageBundle.getString("DismissAll")) {
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

    private fun updateLanguage(language: String) {
        if (properties.getProperty("Language", "en-GB") != language) {
            properties.setProperty("Language", language)
            refreshLanguage()
        }
    }

    private fun refreshLanguage() {
        try {
            // Refresh the language bundle
            val systemLocale = Locale.getDefault().toLanguageTag()
            val locale = Locale.forLanguageTag(properties.getProperty("Language", systemLocale))
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
            val icon = SystemTray.get()
            for (entry in icon.menu.entries) {
                icon.menu.remove(entry)
            }
            createTrayIcon()
        } catch (_: Exception) {
            showError("Failed to set the new language.")
            exit()
        }
    }

    fun setMascotBehaviorEnabled(name: String, mascot: Mascot, enabled: Boolean) {
        val list = mutableListOf<String>()
        val data = properties.getProperty("DisabledBehaviours.${mascot.imageSet}", "").split('/')

        if (data.isNotEmpty() && data[0] != "") {
            list.addAll(data)
        }

        if (list.contains(name) && enabled) {
            list.remove(name)
        } else if (!list.contains(name) && !enabled) {
            list.add(name)
        }

        if (list.isNotEmpty()) {
            properties.setProperty(
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

    private fun toggleBooleanSetting(propertyName: String, defaultValue: Boolean) {
        if (properties.getProperty(propertyName, defaultValue.toString()).toBoolean()) {
            properties.setProperty(propertyName, "false")
        } else {
            properties.setProperty(propertyName, "true")
        }
    }

    private fun setMascotInformationDismissed(imageSet: String) {
        val list = mutableListOf<String>()
        val data = properties.getProperty("InformationDismissed", "").split('/')

        if (data.isNotEmpty() && data[0].isNotEmpty()) {
            list.addAll(data.toList())
        }
        if (!list.contains(imageSet)) {
            list.add(imageSet)
        }

        val value = list.toString()
            .replace("[", "")
            .replace("]", "")
            .replace(", ", "/")

        properties.setProperty("InformationDismissed", value)
    }

    private fun updateConfigFile() {
        runCatching {
            getPath("conf", "settings.properties").outputStream().use {
                properties.store(it, "ShimeLinux Configuration Options")
            }
        }
    }

    private fun setActiveImageSets(newImageSets: MutableList<String>?) {
        if (newImageSets == null) return

        val toRemove = imageSets.toMutableList()

        for (imageSet in toRemove) {
            log.log(Level.INFO, imageSet)
        }

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

                val infoAlreadySeen = properties.getProperty("InformationDismissed", "")
                val alwaysShowInfo = properties.getProperty("AlwaysShowInformationScreen", "false").toBoolean()
                configurations[imageSet]?.let { config ->
                    if (config.containsInformationKey("SplashImage") && (alwaysShowInfo || !infoAlreadySeen.contains(imageSet))
                    ) {
                        val info = InformationWindow(imageSet, config)
                        info.display()
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

        fun createConfigDirectory() {
            try {
                // Create conf directory
                val confDir = getPath("conf")
                if (!confDir.exists() || !confDir.isDirectory()) {
                    confDir.createDirectories()

                    // Copy actions.xml
                    confDir.resolve("actions.xml").outputStream().use { output ->
                        this::class.java.getResourceAsStream("/conf/actions.xml")?.use { input ->
                            input.copyTo(output)
                        }
                    }

                    // Copy behaviors.xml
                    confDir.resolve("behaviors.xml").outputStream().use { output ->
                        this::class.java.getResourceAsStream("/conf/behaviors.xml")?.use { input ->
                            input.copyTo(output)
                        }
                    }

                    // Copy settings.properties
                    confDir.resolve("settings.properties").outputStream().use { output ->
                        this::class.java.getResourceAsStream("/conf/settings.properties")?.use { input ->
                            input.copyTo(output)
                        }
                    }
                }

                // Create img directory
                val imgDir = getPath("img")
                if (!imgDir.exists() || !imgDir.isDirectory()) {
                    // Create unused directory
                    val unusedDir = imgDir.resolve("unused")
                    unusedDir.createDirectories()

                    // Copy default mascot
                    val defaultMascotDir = imgDir.resolve("Shimeji")
                    defaultMascotDir.createDirectories()
                    for (i in 1 until 47) {
                        getPath("img", "Shimeji", "shime$i.png").outputStream().use { output ->
                            this::class.java.getResourceAsStream("/img/Shimeji/shime$i.png")?.use { input ->
                                input.copyTo(output)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                showError("Failed to create the config directory.", e)
                exitProcess(0)
            }
        }

        fun configureLogging() {
            try {
                this::class.java.getResourceAsStream("/conf/logging.properties").use {
                    LogManager.getLogManager().readConfiguration(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun getPath(vararg paths: String): Path {
            val dir = Path(System.getProperty("user.home"), ".config", "shimelinux")
            return Path(dir.toString(), *paths)
        }

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

            showError(m + "\n${instance.languageBundle.getString("SeeLogForDetails")}")
        }
    }
}
