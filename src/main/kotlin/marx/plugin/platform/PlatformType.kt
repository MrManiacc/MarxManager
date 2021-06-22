package marx.plugin.platform

import com.intellij.openapi.roots.libraries.LibraryKind
import marx.plugin.module.modules.AssetModuleType
import marx.plugin.module.modules.MarxModuleType
import marx.plugin.module.modules.RootModuleType
import marx.plugin.module.modules.SourceModuleType
import marx.plugin.utils.ASSET_LIBRARY_KIND
import marx.plugin.utils.MODULE_LIBRARY_KIND
import marx.plugin.utils.ROOT_LIBRARY_KIND
import marx.plugin.utils.SOURCE_LIBRARY_KIND

/**
 * This helps to keep track of all of the modules typesl
 */
enum class PlatformType(
    val type: AbstractModuleType<*>? = null,
    val id: String,
    val metaFile: String? = null,
    private val parent: PlatformType? = null
) {
    MARX_ROOT(RootModuleType(), RootModuleType().id, "project.marx"),
    MARX_MODULE(null, MarxModuleType().id, "module.marx", MARX_ROOT),
    MARX_ASSETS(AssetModuleType(), AssetModuleType().id, "assets.marx", MARX_MODULE),
    MARX_SOURCES(SourceModuleType(), SourceModuleType().id, "sources.marx", MARX_MODULE);

    val children = mutableListOf<PlatformType>()

    init {
        parent?.addChild(this)
    }

    private fun addChild(child: PlatformType) {
        children += child
        parent?.addChild(child)
    }

    companion object {
        fun removeParents(types: MutableSet<PlatformType>) =
            types.filter { type -> type.children.isEmpty() || types.none { type.children.contains(it) } }.toHashSet()

        fun fromLibraryKind(kind: LibraryKind) = when (kind) {
            ROOT_LIBRARY_KIND -> MARX_ROOT
            MODULE_LIBRARY_KIND -> MARX_MODULE
            SOURCE_LIBRARY_KIND -> MARX_SOURCES
            ASSET_LIBRARY_KIND -> MARX_ASSETS
            else -> null
        }
    }

}