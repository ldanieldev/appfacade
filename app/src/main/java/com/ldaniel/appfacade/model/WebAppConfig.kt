package com.ldaniel.appfacade.model

import kotlinx.serialization.Serializable

@Serializable
data class WebAppConfig(
    val id: String,
    val name: String,
    val url: String,
    val iconPath: String? = null,
    val requireUnlock: Boolean = false,
    val fullscreen: Boolean = true,
    val iconSource: String? = null,   // manual override: selfh.st slug or direct image URL
    val iconStyle: String = "auto",   // plate style: auto | white | black | full
    val avoidCutout: Boolean = false,   // fullscreen only: letterbox the display cutout
    val pullToRefresh: Boolean = true,
    val allowCamera: Boolean = false,
    val allowMicrophone: Boolean = false,
    val allowLocation: Boolean = false,
    val orientation: String = WebOrientation.AUTO,
)

fun WebAppConfig.allows(p: SitePermission): Boolean = when (p) {
    SitePermission.CAMERA -> allowCamera
    SitePermission.MICROPHONE -> allowMicrophone
    SitePermission.LOCATION -> allowLocation
}
