package com.ldaniel.appfacade.model

import android.content.pm.ActivityInfo

object WebOrientation {
    const val AUTO = "auto"
    const val PORTRAIT = "portrait"
    const val LANDSCAPE = "landscape"

    fun toRequested(value: String): Int = when (value) {
        PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
}
