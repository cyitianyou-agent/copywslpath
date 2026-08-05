package com.cyitianyou.copywslpath

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBCheckBox
import java.awt.GridLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Provides the application settings page for menu visibility and success
 * notification preferences.
 */
class CopyWslPathConfigurable : Configurable {
    private var panel: JPanel? = null
    private var showNotificationBox: JBCheckBox? = null
    private var showCopyPathMenuBox: JBCheckBox? = null
    private var showCopyRealPathMenuBox: JBCheckBox? = null
    private var showCopyWslPathMenuBox: JBCheckBox? = null
    private var showCopyRealWslPathMenuBox: JBCheckBox? = null

    override fun getDisplayName(): String = "Copy WSL Path"

    override fun createComponent(): JComponent {
        val rootPanel = JPanel(GridLayout(0, 1))
        val showNotification = JBCheckBox("Show notification")
        val showCopyPathMenu = JBCheckBox("Show Copy Path menu")
        val showCopyRealPathMenu = JBCheckBox("Show Copy Real Path menu")
        val showCopyWslPathMenu = JBCheckBox("Show Copy WSL Path menu")
        val showCopyRealWslPathMenu = JBCheckBox("Show Copy Real WSL Path menu")

        rootPanel.add(showNotification)
        rootPanel.add(showCopyPathMenu)
        rootPanel.add(showCopyRealPathMenu)
        rootPanel.add(showCopyWslPathMenu)
        rootPanel.add(showCopyRealWslPathMenu)

        panel = rootPanel
        showNotificationBox = showNotification
        showCopyPathMenuBox = showCopyPathMenu
        showCopyRealPathMenuBox = showCopyRealPathMenu
        showCopyWslPathMenuBox = showCopyWslPathMenu
        showCopyRealWslPathMenuBox = showCopyRealWslPathMenu

        reset()
        return rootPanel
    }

    override fun isModified(): Boolean {
        if (panel == null) {
            return false
        }

        val settings = CopyWslPathSettings.getInstance()

        return showNotificationBox?.isSelected != settings.showNotification ||
            showCopyPathMenuBox?.isSelected != settings.showCopyPathMenu ||
            showCopyRealPathMenuBox?.isSelected != settings.showCopyRealPathMenu ||
            showCopyWslPathMenuBox?.isSelected != settings.showCopyWslPathMenu ||
            showCopyRealWslPathMenuBox?.isSelected != settings.showCopyRealWslPathMenu
    }

    override fun apply() {
        val settings = CopyWslPathSettings.getInstance()

        settings.showNotification = showNotificationBox?.isSelected ?: true
        settings.showCopyPathMenu = showCopyPathMenuBox?.isSelected ?: true
        settings.showCopyRealPathMenu = showCopyRealPathMenuBox?.isSelected ?: true
        settings.showCopyWslPathMenu = showCopyWslPathMenuBox?.isSelected ?: true
        settings.showCopyRealWslPathMenu = showCopyRealWslPathMenuBox?.isSelected ?: true
    }

    override fun reset() {
        val settings = CopyWslPathSettings.getInstance()

        showNotificationBox?.isSelected = settings.showNotification
        showCopyPathMenuBox?.isSelected = settings.showCopyPathMenu
        showCopyRealPathMenuBox?.isSelected = settings.showCopyRealPathMenu
        showCopyWslPathMenuBox?.isSelected = settings.showCopyWslPathMenu
        showCopyRealWslPathMenuBox?.isSelected = settings.showCopyRealWslPathMenu
    }

    override fun disposeUIResources() {
        panel = null
        showNotificationBox = null
        showCopyPathMenuBox = null
        showCopyRealPathMenuBox = null
        showCopyWslPathMenuBox = null
        showCopyRealWslPathMenuBox = null
    }
}
