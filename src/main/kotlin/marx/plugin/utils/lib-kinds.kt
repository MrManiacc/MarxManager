package marx.plugin.utils

import com.intellij.openapi.roots.libraries.LibraryKind

val ROOT_LIBRARY_KIND: LibraryKind = libraryKind("marx-root")
val MODULE_LIBRARY_KIND: LibraryKind = libraryKind("marx-module")
val SOURCE_LIBRARY_KIND: LibraryKind = libraryKind("sources")
val ASSET_LIBRARY_KIND: LibraryKind = libraryKind("assets")

val MARX_LIBRARY_KINDS = setOf(
    ROOT_LIBRARY_KIND,
    MODULE_LIBRARY_KIND,
    SOURCE_LIBRARY_KIND,
    ASSET_LIBRARY_KIND,
)
