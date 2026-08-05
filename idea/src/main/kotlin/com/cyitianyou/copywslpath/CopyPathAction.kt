package com.cyitianyou.copywslpath

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.ui.Messages
import java.awt.datatransfer.StringSelection

/**
 * Shared implementation for the four Project View actions.
 *
 * The mode and visibility selector are injected by each concrete action so
 * the path pipeline, clipboard behavior, error handling, and menu refresh
 * rules remain identical across all commands.
 */
abstract class BaseCopyPathAction(
    private val mode: CopyMode,
    private val isMenuEnabled: CopyWslPathSettings.() -> Boolean
) : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        val virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE)
        val virtualFiles = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        val hasSingleResource = virtualFile != null && (virtualFiles == null || virtualFiles.size == 1)
        val settings = CopyWslPathSettings.getInstance()

        event.presentation.isVisible = virtualFile != null && settings.isMenuEnabled()
        event.presentation.isEnabled = hasSingleResource
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        val virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE)

        try {
            val rawPath = virtualFile?.path
                ?: throw IllegalArgumentException("No resource selected from Project View.")
            val resultPath = buildResultPath(rawPath)

            CopyPasteManager.getInstance().setContents(StringSelection(resultPath))
            notifySuccess(project, resultPath)
        } catch (error: Exception) {
            val message = error.message ?: error::class.simpleName.orEmpty()
            Messages.showErrorDialog(project, message, "Copy WSL Path")
        }
    }

    private fun buildResultPath(rawPath: String): String {
        val selectedPath = when (mode) {
            CopyMode.RAW,
            CopyMode.WSL -> rawPath

            CopyMode.REAL,
            CopyMode.REAL_WSL -> PathResolver.resolveRealPath(rawPath)
        }

        return when (mode) {
            CopyMode.RAW,
            CopyMode.REAL -> selectedPath

            CopyMode.WSL,
            CopyMode.REAL_WSL -> PathConverter.toWslPath(selectedPath)
        }
    }

    private fun notifySuccess(project: com.intellij.openapi.project.Project?, resultPath: String) {
        if (!CopyWslPathSettings.getInstance().showNotification) {
            return
        }

        val title = when (mode) {
            CopyMode.RAW -> "Path Copied"
            CopyMode.REAL -> "Real Path Copied"
            CopyMode.WSL -> "WSL Path Copied"
            CopyMode.REAL_WSL -> "Real WSL Path Copied"
        }
        val notification = NotificationGroupManager
            .getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP_ID)
            .createNotification("$title: $resultPath", NotificationType.INFORMATION)

        notification.notify(project)
    }

    private companion object {
        const val NOTIFICATION_GROUP_ID = "Copy WSL Path"
    }
}
