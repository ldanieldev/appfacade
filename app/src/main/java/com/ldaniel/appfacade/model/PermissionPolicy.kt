package com.ldaniel.appfacade.model

object PermissionPolicy {
    fun isAllowed(p: SitePermission, config: WebAppConfig, held: Set<SitePermission>, origin: String?): Boolean =
        config.allows(p) && p in held && Urls.matchesAppOrigin(config.url, origin)
}
