package marx.plugin.module.modules

import com.intellij.openapi.module.ModuleTypeManager
import marx.plugin.platform.AbstractModule
import marx.plugin.platform.AbstractModuleType
import marx.plugin.platform.PlatformType
import marx.plugin.platform.facet.MarxFacet

/**
 * This is a root module. It allows for multiple children. It also can store global values inside it's project.marx
 * file.
 */
class SourceModule(face: MarxFacet) : AbstractModule(face) {
    override val moduleType: AbstractModuleType<*> = SourceModuleType()
    override val type: PlatformType = PlatformType.MARX_SOURCES
    companion object {
        const val TYPE_ID = "sources"

        private val instance: SourceModuleType
            get() = ModuleTypeManager.getInstance().findByID(TYPE_ID) as SourceModuleType

        operator fun invoke() = instance
    }
}