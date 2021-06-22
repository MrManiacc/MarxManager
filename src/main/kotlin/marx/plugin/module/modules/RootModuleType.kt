package marx.plugin.module.modules

import com.intellij.icons.AllIcons
import marx.plugin.platform.AbstractModuleType
import marx.plugin.platform.PlatformType
import marx.plugin.platform.facet.MarxFacet
import java.awt.Color
import javax.swing.Icon

class RootModuleType(override val groupId: String = "marx.core", override val artifactId: String = "root") :
    AbstractModuleType<RootModule> {
    override val platformType: PlatformType = PlatformType.MARX_ROOT

    override val icon: Icon get() = AllIcons.Nodes.JavaModule

    override val id: String = "RootModule"

    override val ignoredAnnotations: List<String>
        get() = emptyList()
    override val listenerAnnotations: List<String>
        get() = emptyList()

    /**
     * This will generate our actual mode using the facet information
     */
    override fun generateModule(facet: MarxFacet): RootModule {
        return RootModule(facet)
    }

    override val colorMap: LinkedHashMap<String, Color> = LinkedHashMap()

}