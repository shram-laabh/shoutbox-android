package com.shoutboxapp.shoutbox

import android.content.Context

object PermissionManagerSingleton {
    private var _permissionManager: PermissionManager? = null

    val permissionManager: PermissionManager
        get() = _permissionManager ?: throw IllegalStateException("PermissionManager not initialized")

    fun init(context: Context) {
        if (_permissionManager == null) {
            _permissionManager = PermissionManager(context.applicationContext)
        }
    }
}
