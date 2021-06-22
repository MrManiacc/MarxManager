package marx.plugin.module.modules

import com.intellij.icons.AllIcons
import marx.plugin.platform.AbstractModuleType
import marx.plugin.platform.PlatformType
import marx.plugin.platform.facet.MarxFacet
import java.awt.Color
import javax.swing.Icon

class AssetModuleType : AbstractModuleType<AssetModule> {
    override val platformType: PlatformType = PlatformType.MARX_ASSETS

    override val icon: Icon get() = AllIcons.Nodes.ResourceBundle

    override val id: String = "AssetModule"

    override val ignoredAnnotations: List<String>
        get() = emptyList()
    override val listenerAnnotations: List<String>
        get() = emptyList()

    /**
     * This will generate our actual mode using the facet information
     */
    override fun generateModule(facet: MarxFacet): AssetModule {
        return AssetModule(facet)
    }

    override val groupId: String = "marx.core"
    override val artifactId: String = "assets"
    override val colorMap: LinkedHashMap<String, Color> = LinkedHashMap()


}