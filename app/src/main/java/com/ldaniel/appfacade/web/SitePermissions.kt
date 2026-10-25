package com.ldaniel.appfacade.web

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.ldaniel.appfacade.model.SitePermission

object SitePermissions {
    fun held(context: Context): Set<SitePermission> =
        SitePermission.entries.filterTo(mutableSetOf()) { p ->
            val perms = manifestPermissions(p)
            if (p == SitePermission.LOCATION) perms.any { granted(context, it) }
            else perms.all { granted(context, it) }
        }

    fun manifestPermissions(p: SitePermission): Array<String> = when (p) {
        SitePermission.CAMERA -> arrayOf(Manifest.permission.CAMERA)
        SitePermission.MICROPHONE -> arrayOf(Manifest.permission.RECORD_AUDIO)
        SitePermission.LOCATION -> arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }

    private fun granted(context: Context, permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}
