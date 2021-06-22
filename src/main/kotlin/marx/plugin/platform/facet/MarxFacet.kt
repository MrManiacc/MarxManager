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

import com.google.common.collect.HashMultimap
import com.intellij.facet.Facet
import com.intellij.facet.FacetManager
import com.intellij.facet.FacetTypeId
import com.intellij.facet.FacetTypeRegistry
import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleGrouper
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import marx.plugin.platform.AbstractModule
import marx.plugin.platform.AbstractModuleType
import marx.plugin.platform.PlatformType
import marx.plugin.platform.SourceType
import marx.plugin.platform.facet.MarxFacetType.Companion.TYPE_ID
import marx.plugin.resource.Icons
import marx.plugin.utils.mapFirstNotNull
import org.jetbrains.jps.model.java.JavaResourceRootType
import org.jetbrains.jps.model.java.JavaSourceRootType
import java.util.concurrent.ConcurrentHashMap
import javax.swing.Icon

class MarxFacet(
    module: Module,
    name: String,
    configuration: MarxFacetConfig,
    underlyingFacet: Facet<*>?
) : Facet<MarxFacetConfig>(facetType, module, name, configuration, underlyingFacet) {

    private val moduleMap = ConcurrentHashMap<AbstractModuleType<*>, AbstractModule>()
    private val roots: HashMultimap<SourceType, VirtualFile?> = HashMultimap.create()

    init {
        configuration.facet = this
    }

    override fun initFacet() {
        refresh()
    }

    override fun disposeFacet() {
        moduleMap.forEach { (_, m) ->
            m.dispose()
        }
        moduleMap.clear()
        roots.clear()
    }

    fun refresh() {
        if (module.isDisposed) {
            return
        }
        // Don't allow parent types with child types in auto detected set
        val allEnabled = configuration.state.run {
            modulePlatforms = PlatformType.removeParents(modulePlatforms)

            val userEnabled = userChosenTypes.entries.asSequence()
                .filter { it.value }
                .map { it.key }

            val autoEnabled = modulePlatforms.asSequence()
                .filter { userChosenTypes[it] == null }

            userEnabled + autoEnabled
        }

        // Remove modules that aren't registered anymore
        val toBeRemoved = moduleMap.entries.asSequence()
            .filter { !allEnabled.contains(it.key.platformType) }
            .onEach { it.value.dispose() }
            .map { it.key }
            .toHashSet() // CME defense
        toBeRemoved.forEach { moduleMap.remove(it) }

        // Do this before we register the new modules
        updateRoots()

        // Add modules which are new
        val newlyEnabled = mutableListOf<AbstractModule>()
        allEnabled
            .map { it.type }
            .filter { !moduleMap.containsKey(it) }
            .forEach {
                it ?: return@forEach
                newlyEnabled += register(it)
            }

        newlyEnabled.forEach(AbstractModule::init)

        ProjectView.getInstance(module.project).refresh()
    }

    private fun updateRoots() {
        roots.clear()
        val rootManager = ModuleRootManager.getInstance(module)

        rootManager.contentEntries.asSequence()
            .flatMap { entry -> entry.sourceFolders.asSequence() }
            .filterNotNull()
            .forEach {
                when (it.rootType) {
                    JavaSourceRootType.SOURCE -> roots.put(SourceType.SOURCE, it.file)
                    JavaSourceRootType.TEST_SOURCE -> roots.put(SourceType.TEST_SOURCE, it.file)
                    JavaResourceRootType.RESOURCE -> roots.put(SourceType.RESOURCE, it.file)
                    JavaResourceRootType.TEST_RESOURCE -> roots.put(SourceType.TEST_RESOURCE, it.file)
                }
            }
    }

    private fun register(type: AbstractModuleType<*>): AbstractModule {
        type.performCreationSettingSetup(module.project)
        val module = type.generateModule(this)
        moduleMap[type] = module
        return module
    }

    val modules get() = moduleMap.values
    val types get() = moduleMap.keys

    fun isOfType(type: AbstractModuleType<*>) = moduleMap.containsKey(type)

    fun <T : AbstractModule> getModuleOfType(type: AbstractModuleType<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return moduleMap[type] as? T
    }

    fun isEventClassValidForModule(eventClass: PsiClass?) =
        eventClass != null && moduleMap.values.any { it.isEventClassValid(eventClass, null) }

    fun isEventClassValid(eventClass: PsiClass, method: PsiMethod): Boolean {
        return doIfGood(method) {
            it.isEventClassValid(eventClass, method)
        } == true
    }

    fun writeErrorMessageForEvent(eventClass: PsiClass, method: PsiMethod): String? {
        return doIfGood(method) {
            it.writeErrorMessageForEventParameter(eventClass, method)
        }
    }

    fun isStaticListenerSupported(method: PsiMethod): Boolean {
        return doIfGood(method) {
            it.isStaticListenerSupported(method)
        } == true
    }

    fun suppressStaticListener(method: PsiMethod): Boolean {
        return doIfGood(method) {
            !it.isStaticListenerSupported(method)
        } == true
    }

    private inline fun <T> doIfGood(method: PsiMethod, action: (AbstractModule) -> T): T? {
        for (abstractModule in moduleMap.values) {
            val good = abstractModule.moduleType.listenerAnnotations.any {
                method.modifierList.findAnnotation(it) != null
            }

            if (good) {
                return action(abstractModule)
            }
        }
        return null
    }

    val isEventGenAvailable get() = moduleMap.keys.any { it.isEventGenAvailable }

    fun shouldShowPluginIcon(element: PsiElement?) = moduleMap.values.any { it.shouldShowPluginIcon(element) }

    val icon: Icon?
        get() {
            val iconCount = moduleMap.keys.count { it.hasIcon }
            return when {
                iconCount == 0 -> null
                iconCount == 1 -> moduleMap.keys.firstOrNull { it.hasIcon }?.icon
                moduleMap.size > 0 -> Icons.`code-file-32`
                else -> null
            }
        }

    fun findFile(path: String, type: SourceType): VirtualFile? {
        try {
            return findFile0(path, type)
        } catch (ignored: RefreshRootsException) {
        }

        updateRoots()

        return try {
            findFile0(path, type)
        } catch (ignored: RefreshRootsException) {
            // Well we tried our best
            null
        }
    }

    private class RefreshRootsException : Exception()

    @Throws(RefreshRootsException::class)
    private fun findFile0(path: String, type: SourceType): VirtualFile? {
        val roots = roots[type]

        for (root in roots) {
            val r = root ?: continue
            if (!r.isValid) {
                throw RefreshRootsException()
            }
            return r.findFileByRelativePath(path) ?: continue
        }

        return null
    }

    companion object {
        val ID = FacetTypeId<MarxFacet>(TYPE_ID)
        val facetType get() = FacetTypeRegistry.getInstance().findFacetType(ID) as MarxFacetType

        fun getChildInstances(module: Module) = runReadAction run@{
            val instance = MarxFacet(module)
            if (instance != null) {
                return@run setOf(instance)
            }

            val project = module.project
            val manager = ModuleManager.getInstance(project)
            val grouper = ModuleGrouper.instanceFor(project)

            val result = mutableSetOf<MarxFacet>()

            val modulePath = grouper.getModuleAsGroupPath(module) ?: return@run result

            for (m in manager.modules) {
                val path = grouper.getGroupPath(m)
                if (modulePath != path) {
                    continue
                }

                val facet = MarxFacet(m) ?: continue
                result.add(facet)
            }
            return@run result
        }

        operator fun invoke(module: Module) = FacetManager.getInstance(module).getFacetByType(ID)

        operator fun <T : AbstractModule> invoke(module: Module, type: AbstractModuleType<T>) =
            MarxFacet(module)?.getModuleOfType(type)

        operator fun <T : AbstractModule> invoke(module: Module, vararg types: AbstractModuleType<T>): T? {
            val instance = MarxFacet(module) ?: return null
            return types.mapFirstNotNull { instance.getModuleOfType(it) }
        }
    }
}
