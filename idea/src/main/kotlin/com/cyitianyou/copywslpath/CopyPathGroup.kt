package com.cyitianyou.copywslpath

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup

/**
 * Groups all path-copy commands into one Project View submenu.
 *
 * The group reads the same persisted menu settings as its child actions. This
 * prevents an empty submenu from remaining visible after all four commands
 * have been disabled in the plugin settings.
 */
class CopyPathGroup : DefaultActionGroup() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        val settings = CopyWslPathSettings.getInstance()
        val hasVisibleAction = settings.showCopyPathMenu ||
            settings.showCopyRealPathMenu ||
            settings.showCopyWslPathMenu ||
            settings.showCopyRealWslPathMenu

        event.presentation.isVisible = hasVisibleAction
        event.presentation.isEnabled = hasVisibleAction
    }
}
