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
    private val imagePanel: JPanel
    private val splashImageLabel: JLabel
    private val editorPane: JEditorPane
    private val footerPanel: JPanel
    private val closeButton: JButton

    init {
        val icon = loadResource("/img/icon.png").use { ImageIO.read(it) }
        iconImage = icon
        title = if (config.containsInformationKey("Name")) config.getInformation("Name") else "Information".localize()
        isResizable = false
        defaultCloseOperation = DISPOSE_ON_CLOSE
        layout = BoxLayout(contentPane, BoxLayout.Y_AXIS)

        val splashImage = requireNotNull(config.getInformation("SplashImage"))
        splashImageLabel = JLabel()
        splashImageLabel.alignmentX = CENTER_ALIGNMENT
        splashImageLabel.icon = ImageIcon(getPath("img", imageSet, splashImage).toString())

        imagePanel = JPanel()
        imagePanel.layout = BoxLayout(imagePanel, BoxLayout.Y_AXIS)
        imagePanel.add(splashImageLabel)

        closeButton = JButton("Close".localize())
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
                append("ArtBy".localize()).append(" ")
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

                append("ScriptedBy".localize()).append(" ")
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

                append("CommissionedBy".localize()).append(" ")
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

                append("SupportAt".localize()).append(" ")
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
                    "ConfirmVisitWebsiteMessage".localize() + "\n" +
                    "ExerciseCautionAndBewareSusLinksMessage".localize() + "\n$url",
                    "VisitWebsite".localize(),
                    JOptionPane.YES_NO_OPTION
                )

                if (response == JOptionPane.YES_OPTION) {
                    val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
                    try {
                        checkNotNull(desktop).browse(URI(url))
                    } catch (_: Exception) {
                        JOptionPane.showMessageDialog(
                            this@InformationWindow,
                            "FailedOpenWebBrowserErrorMessage".localize() + "\n$url",
                            "Error",
                            JOptionPane.PLAIN_MESSAGE
                        )
                    }
                }
            }
        }
    }
}
