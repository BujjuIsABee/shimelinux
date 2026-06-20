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
    init {
        val lang = Main.instance.languageBundle

        val splashImage = requireNotNull(config.getInformation("SplashImage"))
        val splashImageLabel = JLabel()
        splashImageLabel.alignmentX = CENTER_ALIGNMENT
        splashImageLabel.icon = ImageIcon(Main.getPath("img", imageSet, splashImage).toString())

        val imagePanel = JPanel()
        imagePanel.layout = BoxLayout(imagePanel, BoxLayout.Y_AXIS)
        imagePanel.add(splashImageLabel)

        val editorPane = JEditorPane()
        editorPane.border = null
        editorPane.isEditable = false
        editorPane.contentType = "text/html"

        val closeButton = JButton(lang.getString("Close"))
        closeButton.minimumSize = Dimension(95, 23)
        closeButton.maximumSize = Dimension(130, 26)
        closeButton.preferredSize = Dimension(130, 26)
        closeButton.addActionListener { dispose() }

        val footerPanel = JPanel(FlowLayout(FlowLayout.CENTER, 10, 5))
        footerPanel.preferredSize = Dimension(380, 36)
        footerPanel.add(closeButton)

        val textColor = UIManager.getColor("Label.foreground")
        val linkColor = UIManager.getColor("textHighlight")

        val html = StringBuilder("<center style=\"font:")
        when (splashImageLabel.font.style) {
            Font.BOLD -> html.append("bold ")
            Font.ITALIC -> html.append("italic ")
            Font.BOLD + Font.ITALIC -> html.append("italic bold ")
        }

        html.append(splashImageLabel.font.size).append("pt ")
        html.append(splashImageLabel.font.fontName).append("; ")
        html.append(
            String.format(
                "#%02X%02X%02X",
                textColor.red,
                textColor.green,
                textColor.blue
            )
        )
        html.append("\">")

        if (config.containsInformationKey("ArtistName")) {
            html.append(lang.getString("ArtBy")).append(" ")
            if (config.containsInformationKey("ArtistURL")) {
                html.append("<a href=\"").append(config.getInformation("ArtistURL"))
                html.append("\" style=\"color:")
                html.append(
                    String.format(
                        "#%02X%02X%02X",
                        linkColor.red,
                        linkColor.green,
                        linkColor.blue
                    )
                )
                html.append("\">")
            }
            html.append(config.getInformation("ArtistName"))
            if (config.containsInformationKey("ArtistURL")) {
                html.append("</a>")
            }
        }

        if (config.containsInformationKey("ScripterName")) {
            if (config.containsInformationKey("ArtistName")) {
                html.append(" - ")
            }

            html.append(lang.getString("ScriptedBy")).append(" ")
            if (config.containsInformationKey("ScripterURL")) {
                html.append("<a href=\"").append(config.getInformation("ScripterURL"))
                html.append("\" style=\"color:")
                html.append(
                    String.format(
                        "#%02X%02X%02X",
                        linkColor.red,
                        linkColor.green,
                        linkColor.blue
                    )
                )
                html.append("\">")
            }
            html.append(config.getInformation("ScripterName"))
            if (config.containsInformationKey("ScripterURL")) {
                html.append("</a>")
            }
        }

        if (config.containsInformationKey("CommissionerName")) {
            if (config.containsInformationKey("ArtistName") ||
                config.containsInformationKey("ScripterName")
            ) {
                html.append(" - ")
            }

            html.append(lang.getString("CommissionedBy")).append(" ")
            if (config.containsInformationKey("CommissionerURL")) {
                html.append("<a href=\"").append(config.getInformation("CommissionerURL"))
                html.append("\" style=\"color:")
                html.append(
                    String.format(
                        "#%02X%02X%02X",
                        linkColor.red,
                        linkColor.green,
                        linkColor.blue
                    )
                )
                html.append("\">")
            }
            html.append(config.getInformation("CommissionerName"))
            if (config.containsInformationKey("CommissionerURL")) {
                html.append("</a>")
            }
        }

        if (config.containsInformationKey("SupportName")) {
            if (config.containsInformationKey("ArtistName") ||
                config.containsInformationKey("ScripterName") ||
                config.containsInformationKey("CommissionerName")
            ) {
                html.append(" - ")
            }

            html.append(lang.getString("SupportAt")).append(" ")
            if (config.containsInformationKey("SupportURL")) {
                html.append("<a href=\"").append(config.getInformation("SupportURL"))
                html.append("\" style=\"color:")
                html.append(
                    String.format(
                        "#%02X%02X%02X",
                        linkColor.red,
                        linkColor.green,
                        linkColor.blue
                    )
                )
                html.append("\">")
            }
            html.append(config.getInformation("SupportName"))
            if (config.containsInformationKey("SupportURL")) {
                html.append("</a>")
            }
        }
        html.append("</center>")

        editorPane.text = html.toString()
        editorPane.addHyperlinkListener { event ->
            if (event.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                val st = StringTokenizer(event.description, " ")
                if (st.hasMoreTokens()) {
                    val url = st.nextToken()
                    if (JOptionPane.showConfirmDialog(
                            this@InformationWindow,
                            lang.getString("ConfirmVisitWebsiteMessage") + "\n" +
                            lang.getString("ExerciseCautionAndBewareSusLinksMessage") + "\n$url",
                            lang.getString("VisitWebsite"),
                            JOptionPane.YES_NO_OPTION
                        ) == JOptionPane.YES_OPTION
                    ) {
                        val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
                        var failed = false
                        try {
                            if (desktop != null) {
                                desktop.browse(URI(url))
                            } else {
                                failed = true
                            }
                        } catch (_: Exception) {
                            failed = true
                        }

                        if (failed) {
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

        val icon = this::class.java.getResourceAsStream("/img/icon.png").use { ImageIO.read(it) }
        iconImage = icon
        title = if (config.containsInformationKey("Name")) config.getInformation("Name") else lang.getString("Information")
        defaultCloseOperation = DISPOSE_ON_CLOSE
        contentPane.layout = BoxLayout(contentPane, BoxLayout.Y_AXIS)
        add(imagePanel)
        add(JScrollPane(editorPane))
        add(footerPanel)
        pack()
        setLocationRelativeTo(null)
    }
}
