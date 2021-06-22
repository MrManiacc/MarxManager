/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package marx.plugin.platform.facet

import com.intellij.facet.Facet
import com.intellij.facet.FacetType
import com.intellij.openapi.module.JavaModuleType
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType
import marx.plugin.resource.Icons

class MarxFacetType :
    FacetType<MarxFacet, MarxFacetConfig>(MarxFacet.ID, TYPE_ID, "Marx") {

    override fun createFacet(
        module: Module,
        name: String,
        configuration: MarxFacetConfig,
        underlyingFacet: Facet<*>?
    ) = MarxFacet(module, name, configuration, underlyingFacet)

    override fun createDefaultConfiguration() = MarxFacetConfig()
    override fun isSuitableModuleType(moduleType: ModuleType<*>?) = moduleType is JavaModuleType

    override fun getIcon() = Icons.`code-file-32`

    companion object {
        const val TYPE_ID = "marx"
    }
}
