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
class MarxModule(face: MarxFacet) : AbstractModule(face) {
    override val moduleType: AbstractModuleType<*> = AssetModule()
    override val type: PlatformType = PlatformType.MARX_MODULE


    companion object {
        const val TYPE_ID = "module"

        private val instance: MarxModuleType
            get() = ModuleTypeManager.getInstance().findByID(TYPE_ID) as MarxModuleType

        operator fun invoke() = instance
    }
}