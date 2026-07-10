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
)
