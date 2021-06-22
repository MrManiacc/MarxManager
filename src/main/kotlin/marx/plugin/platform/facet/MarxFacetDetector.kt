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

import com.intellij.facet.FacetManager
import com.intellij.facet.impl.ui.libraries.LibrariesValidatorContextImpl
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootEvent
import com.intellij.openapi.roots.ModuleRootListener
import com.intellij.openapi.roots.libraries.LibraryKind
import com.intellij.openapi.roots.ui.configuration.libraries.LibraryPresentationManager
import com.intellij.openapi.startup.StartupActivity
import marx.plugin.platform.PlatformType
import marx.plugin.utils.MARX_LIBRARY_KINDS
import marx.plugin.utils.MODULE_LIBRARY_KIND
import marx.plugin.utils.runWriteTaskLater

class MarxFacetDetector : StartupActivity {

    override fun runActivity(project: Project) {
        MarxModuleRootListener.doCheck(project)
    }

    private object MarxModuleRootListener : ModuleRootListener {
        override fun rootsChanged(event: ModuleRootEvent) {
            if (event.isCausedByFileTypesChange) {
                return
            }

            val project = event.source as? Project ?: return
            doCheck(project)
        }

        fun doCheck(project: Project) {
            val moduleManager = ModuleManager.getInstance(project)
            for (module in moduleManager.modules) {
                val facetManager = FacetManager.getInstance(module)
                val marxFacet = facetManager.getFacetByType(MarxFacet.ID)

                if (marxFacet == null) {
                    checkNoFacet(module)
                } else {
                    checkExistingFacet(module, marxFacet)
                }
            }
        }

        private fun checkNoFacet(module: Module) {
            val platforms = autoDetectTypes(module).ifEmpty { return }

            val facetManager = FacetManager.getInstance(module)
            val configuration = MarxFacetConfig()
            configuration.state.modulePlatforms.addAll(platforms)

            val facet = facetManager.createFacet(MarxFacet.facetType, "Marx", configuration, null)
            runWriteTaskLater {
                // Only add the new facet if there isn't a Minecraft facet already - double check here since this
                // task may run much later
                if (module.isDisposed || facet.isDisposed) {
                    // Module may be disposed before we run
                    return@runWriteTaskLater
                }
                if (facetManager.getFacetByType(MarxFacet.ID) == null) {
                    val model = facetManager.createModifiableModel()
                    model.addFacet(facet)
                    model.commit()
                }
            }
        }

        private fun checkExistingFacet(module: Module, facet: MarxFacet) {
            val platforms = autoDetectTypes(module).ifEmpty { return }

            val types = facet.configuration.state.modulePlatforms
            types.clear()
            types.addAll(platforms)

            if (facet.configuration.state.useAssets) {
                types.add(PlatformType.MARX_ASSETS)
            }

            if (facet.configuration.state.useSources) {
                types.add(PlatformType.MARX_SOURCES)
            }

            if (facet.configuration.state.useAssets && facet.configuration.state.useSources) {
                types.add(PlatformType.MARX_MODULE)
            }
            facet.refresh()
        }

        private fun autoDetectTypes(module: Module): Set<PlatformType> {
            val presentationManager = LibraryPresentationManager.getInstance()
            val context = LibrariesValidatorContextImpl(module)

            val platformKinds = mutableSetOf<LibraryKind>()
            context.rootModel
                .orderEntries()
                .using(context.modulesProvider)
                .recursively()
                .librariesOnly()
                .forEachLibrary forEach@{ library ->
                    MARX_LIBRARY_KINDS.forEach { kind ->
                        if (presentationManager.isLibraryOfKind(library, context.librariesContainer, setOf(kind))) {
                            platformKinds.add(kind)
                        }
                    }
                    return@forEach true
                }

            context.rootModel
                .orderEntries()
                .using(context.modulesProvider)
                .recursively()
                .withoutLibraries()
                .withoutSdk()
                .forEachModule forEach@{ m ->
                    if (m.name.startsWith("MarxAPI")) {
                        // We don't want want to add parent modules in module groups
                        val moduleManager = ModuleManager.getInstance(m.project)
                        val groupPath = moduleManager.getModuleGroupPath(m)
                        if (groupPath == null) {
                            platformKinds.add(MODULE_LIBRARY_KIND)
                            return@forEach true
                        }

                        val name = groupPath.lastOrNull() ?: return@forEach true
                        if (m.name == name) {
                            return@forEach true
                        }

                        platformKinds.add(MODULE_LIBRARY_KIND)
                    }
                    return@forEach true
                }
            return platformKinds.mapNotNull { kind -> PlatformType.fromLibraryKind(kind) }.toSet()
        }
    }
}
