package marx.plugin.module

import com.intellij.openapi.module.ModuleType
import marx.plugin.resource.Icons
import marx.plugin.resource.Localize
import javax.swing.Icon

/**
 * This allows for the adding of frameworks
 */
@Suppress("UnstableApiUsage")
class MarxModuleType : ModuleType<MarxModuleBuilder>(ID) {

    override fun createModuleBuilder(): MarxModuleBuilder = MarxModuleBuilder()

    override fun getName(): String = Localize("marx.framework.module.displayName")

    override fun getDescription(): String = Localize("marx.framework.module.description")

    override fun getNodeIcon(isOpened: Boolean): Icon = Icons.`code-file-22`

    override fun getIcon(): Icon = Icons.`code-file-22`

    /**
     * This is used soley for the ID/getting the instance
     */
    companion object {
        const val ID = "MarxModuleType"
        private val instance = MarxModuleType()
        operator fun invoke(): ModuleType<*> = instance
    }
}