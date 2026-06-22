/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot

import com.group_finity.mascot.config.Configuration
import java.awt.Desktop
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.net.URI
import java.util.StringTokenizer
import javax.imageio.ImageIO
import javax.swing.BoxLayout
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JEditorPane
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.UIManager
import javax.swing.event.HyperlinkEvent

class InformationWindow(imageSet: String, config: Configuration) : JFrame() {
    private val lang = Main.instance.languageBundle

    private val imagePanel: JPanel
    private val splashImageLabel: JLabel
    private val editorPane: JEditorPane
    private val footerPanel: JPanel
    private val closeButton: JButton

    init {
        val icon = this::class.java.getResourceAsStream("/img/icon.png").use { ImageIO.read(it) }
        iconImage = icon
        title = if (config.containsInformationKey("Name")) config.getInformation("Name") else lang.getString("Information")
        defaultCloseOperation = DISPOSE_ON_CLOSE
        layout = BoxLayout(contentPane, BoxLayout.Y_AXIS)

        val splashImage = requireNotNull(config.getInformation("SplashImage"))
        splashImageLabel = JLabel()
        splashImageLabel.alignmentX = CENTER_ALIGNMENT
        splashImageLabel.icon = ImageIcon(Main.getPath("img", imageSet, splashImage).toString())

        imagePanel = JPanel()
        imagePanel.layout = BoxLayout(imagePanel, BoxLayout.Y_AXIS)
        imagePanel.add(splashImageLabel)

        closeButton = JButton(lang.getString("Close"))
        closeButton.minimumSize = Dimension(95, 23)
        closeButton.maximumSize = Dimension(130, 26)
        closeButton.preferredSize = Dimension(130, 26)
        closeButton.addActionListener { dispose() }

        footerPanel = JPanel(FlowLayout(FlowLayout.CENTER, 10, 5))
        footerPanel.preferredSize = Dimension(380, 36)
        footerPanel.add(closeButton)

        val textColor = UIManager.getColor("Label.foreground")
        val linkColor = UIManager.getColor("textHighlight")

        editorPane = JEditorPane()
        editorPane.border = null
        editorPane.isEditable = false
        editorPane.contentType = "text/html"
        editorPane.text = buildString {
            append("<center ")

            append("style=\"font:")
            when (splashImageLabel.font.style) {
                Font.BOLD -> append("bold ")
                Font.ITALIC -> append("italic ")
                Font.BOLD + Font.ITALIC -> append("italic bold ")
            }

            append(splashImageLabel.font.size).append("pt ")
            append(splashImageLabel.font.fontName).append("; ")
            append(
                String.format(
                    "#%02X%02X%02X",
                    textColor.red,
                    textColor.green,
                    textColor.blue
                )
            )

            append("\">")

            if (config.containsInformationKey("ArtistName")) {
                append(lang.getString("ArtBy")).append(" ")
                if (config.containsInformationKey("ArtistURL")) {
                    append("<a href=\"").append(config.getInformation("ArtistURL")).append("\" ")

                    append("style=\"color:")
                    append(
                        String.format(
                            "#%02X%02X%02X",
                            linkColor.red,
                            linkColor.green,
                            linkColor.blue
                        )
                    )

                    append("\">")
                }

                append(config.getInformation("ArtistName"))
                if (config.containsInformationKey("ArtistURL")) {
                    append("</a>")
                }
            }

            if (config.containsInformationKey("ScripterName")) {
                if (config.containsInformationKey("ArtistName")) {
                    append(" - ")
                }

                append(lang.getString("ScriptedBy")).append(" ")
                if (config.containsInformationKey("ScripterURL")) {
                    append("<a href=\"").append(config.getInformation("ScripterURL")).append("\" ")

                    append("style=\"color:")
                    append(
                        String.format(
                            "#%02X%02X%02X",
                            linkColor.red,
                            linkColor.green,
                            linkColor.blue
                        )
                    )

                    append("\">")
                }

                append(config.getInformation("ScripterName"))
                if (config.containsInformationKey("ScripterURL")) {
                    append("</a>")
                }
            }

            if (config.containsInformationKey("CommissionerName")) {
                if (config.containsInformationKey("ArtistName") ||
                    config.containsInformationKey("ScripterName")
                ) {
                    append(" - ")
                }

                append(lang.getString("CommissionedBy")).append(" ")
                if (config.containsInformationKey("CommissionerURL")) {
                    append("<a href=\"").append(config.getInformation("CommissionerURL")).append("\" ")

                    append("style=\"color:")
                    append(
                        String.format(
                            "#%02X%02X%02X",
                            linkColor.red,
                            linkColor.green,
                            linkColor.blue
                        )
                    )

                    append("\">")
                }

                append(config.getInformation("CommissionerName"))
                if (config.containsInformationKey("CommissionerURL")) {
                    append("</a>")
                }
            }

            if (config.containsInformationKey("SupportName")) {
                if (config.containsInformationKey("ArtistName") ||
                    config.containsInformationKey("ScripterName") ||
                    config.containsInformationKey("CommissionerName")
                ) {
                    append(" - ")
                }

                append(lang.getString("SupportAt")).append(" ")
                if (config.containsInformationKey("SupportURL")) {
                    append("<a href=\"").append(config.getInformation("SupportURL")).append("\" ")

                    append("style=\"color:")
                    append(
                        String.format(
                            "#%02X%02X%02X",
                            linkColor.red,
                            linkColor.green,
                            linkColor.blue
                        )
                    )

                    append("\">")
                }

                append(config.getInformation("SupportName"))
                if (config.containsInformationKey("SupportURL")) {
                    append("</a>")
                }
            }
            append("</center>")
        }
        editorPane.addHyperlinkListener { event -> handleHyperlink(event) }

        add(imagePanel)
        add(JScrollPane(editorPane))
        add(footerPanel)
        pack()
        setLocationRelativeTo(null)
    }

    private fun handleHyperlink(event: HyperlinkEvent) {
        if (event.eventType == HyperlinkEvent.EventType.ACTIVATED) {
            val st = StringTokenizer(event.description, " ")
            if (st.hasMoreTokens()) {
                val url = st.nextToken()
                val response = JOptionPane.showConfirmDialog(
                    this@InformationWindow,
                    lang.getString("ConfirmVisitWebsiteMessage") + "\n" +
                    lang.getString("ExerciseCautionAndBewareSusLinksMessage") + "\n$url",
                    lang.getString("VisitWebsite"),
                    JOptionPane.YES_NO_OPTION
                )

                if (response == JOptionPane.YES_OPTION) {
                    val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
                    try {
                        checkNotNull(desktop).browse(URI(url))
                    } catch (_: Exception) {
                        JOptionPane.showMessageDialog(
                            this@InformationWindow,
                            lang.getString("FailedOpenWebBrowserErrorMessage") + "\n$url",
                            "Error",
                            JOptionPane.PLAIN_MESSAGE
                        )
                    }
                }
            }
        }
    }
}
