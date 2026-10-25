package com.ldaniel.appfacade.model

import android.webkit.PermissionRequest

enum class SitePermission {
    CAMERA, MICROPHONE, LOCATION;

    companion object {
        fun fromWebResource(resource: String): SitePermission? = when (resource) {
            PermissionRequest.RESOURCE_VIDEO_CAPTURE -> CAMERA
            PermissionRequest.RESOURCE_AUDIO_CAPTURE -> MICROPHONE
            else -> null
        }
    }
}
