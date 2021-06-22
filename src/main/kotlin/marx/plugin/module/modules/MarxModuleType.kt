package marx.plugin.module.modules

import com.intellij.openapi.module.JavaModuleType
import com.intellij.openapi.module.ModuleTypeManager
import marx.plugin.module.MarxModuleBuilder
import marx.plugin.module.MarxModuleType
import marx.plugin.resource.Icons
import javax.swing.Icon

class MarxModuleType : JavaModuleType() {
    override fun createModuleBuilder() = MarxModuleBuilder()

    override fun getName(): String = NAME

    override fun getDescription() =
        "Marx modules are used for developing plugins or mods for the <b>Marx Engine</b> "


    override fun getIcon(): Icon = Icons.`code-file-32`

    override fun getNodeIcon(isOpened: Boolean): Icon = Icons.`code-file-32`



    companion object {
        private const val ID = "MARX_MODULE_TYPE"
        const val NAME = "Marx"
        private val instance: MarxModuleType
            get() = ModuleTypeManager.getInstance().findByID(ID) as MarxModuleType

        operator fun invoke() = instance
    }
}