package com.cyitianyou.copywslpath

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * Stores application-level preferences shared by all four Project View
 * actions. Keeping the state in one service ensures menu visibility and
 * notification behavior always read the same persisted values.
 */
@Service(Service.Level.APP)
@State(
    name = "com.cyitianyou.copywslpath.CopyWslPathSettings",
    storages = [Storage("copy-wsl-path.xml")]
)
class CopyWslPathSettings : SimplePersistentStateComponent<CopyWslPathSettings.State>(State()) {
    var showNotification: Boolean
        get() = state.showNotification
        set(value) {
            state.showNotification = value
        }

    var showCopyPathMenu: Boolean
        get() = state.showCopyPathMenu
        set(value) {
            state.showCopyPathMenu = value
        }

    var showCopyRealPathMenu: Boolean
        get() = state.showCopyRealPathMenu
        set(value) {
            state.showCopyRealPathMenu = value
        }

    var showCopyWslPathMenu: Boolean
        get() = state.showCopyWslPathMenu
        set(value) {
            state.showCopyWslPathMenu = value
        }

    var showCopyRealWslPathMenu: Boolean
        get() = state.showCopyRealWslPathMenu
        set(value) {
            state.showCopyRealWslPathMenu = value
        }

    /**
     * XML-serializable state. Explicit public fields keep the persisted
     * format stable and make default values self-documenting.
     */
    class State : com.intellij.openapi.components.BaseState() {
        var showNotification by property(true)

        var showCopyPathMenu by property(true)

        var showCopyRealPathMenu by property(true)

        var showCopyWslPathMenu by property(true)

        var showCopyRealWslPathMenu by property(true)
    }

    companion object {
        fun getInstance(): CopyWslPathSettings =
            ApplicationManager.getApplication().getService(CopyWslPathSettings::class.java)
    }
}
