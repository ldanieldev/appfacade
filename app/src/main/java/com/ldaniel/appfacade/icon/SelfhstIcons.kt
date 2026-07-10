package com.ldaniel.appfacade.icon

object SelfhstIcons {
    /** selfh.st reference name: lowercase, non-alphanumerics collapsed to hyphens. */
    fun slugify(name: String): String =
        name.trim().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')

    /** CDN PNG for a selfh.st icon slug or human name. */
    fun pngUrl(slugOrName: String): String =
        "https://cdn.jsdelivr.net/gh/selfhst/icons/png/${slugify(slugOrName)}.png"
}
