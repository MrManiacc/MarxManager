package marx.plugin.utils

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project

/**returns a [ModuleManager] instance via the [Project] instance**/
val Project.moduleManager: ModuleManager get() = ModuleManager.getInstance(this)

/**returns an array of [Module] for the given project**/
val Project.modules: Array<out Module> get() = moduleManager.modules

/**returns a sorted array of [Module] for the given project**/
val Project.sortedModules: Array<out Module> get() = moduleManager.sortedModules

/**returns a sorted array of [Module] for the given project**/
val Project.moduleCount: Int get() = modules.size


/**@return the service for the given module of type [T]. Creates new instance if null.**/
inline fun <reified T : Any> Module.service(): T = this.getService(T::class.java)

/**@return the service for the given module of type [T]. Returns null if not created..**/
inline fun <reified T : Any> Module.serviceIfCreated(): T? = getServiceIfCreated(T::class.java)


/**A module that takes in the various variables needed to make a module**/
typealias ModuleData = (group: String, artifact: String, version: String) -> Unit



