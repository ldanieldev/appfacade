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
)
