package marx.plugin.platform.facet

import com.intellij.facet.FacetConfiguration
import com.intellij.facet.ui.FacetEditorContext
import com.intellij.facet.ui.FacetValidatorsManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.XCollection
import marx.plugin.platform.PlatformType

class MarxFacetConfig : FacetConfiguration,
    PersistentStateComponent<MarxFacetConfig.MarxFacetData> {

    var facet: MarxFacet? = null
    private var state = MarxFacetData()

    override fun createEditorTabs(editorContext: FacetEditorContext?, validatorsManager: FacetValidatorsManager?) =
        arrayOf(MarxFacetEditorTab(this))

    override fun getState() = state
    override fun loadState(state: MarxFacetData) {
        this.state = state
    }

    data class MarxFacetData(
        @Tag("userChosenTypes")
        var userChosenTypes: MutableMap<PlatformType, Boolean> = mutableMapOf(),
        @Tag("autoDetectTypes")
        @XCollection(elementName = "platformType", valueAttributeName = "", style = XCollection.Style.v2)
        var modulePlatforms: MutableSet<PlatformType> = mutableSetOf(),
        @Tag("assets")
        var useAssets: Boolean = false,
        @Tag("sources")
        var useSources: Boolean = true,
        )

}

