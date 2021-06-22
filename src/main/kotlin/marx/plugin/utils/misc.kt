package marx.plugin.utils

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.libraries.LibraryKind
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import java.util.*

/**
 * This will get the given value of the [Optional] or throw an [IllegalStateException] if
 * the value isn't present and the default value isn't preset
 */
@Throws(IllegalStateException::class)
inline operator fun <reified T : Any> Optional<T>.invoke(default: T? = null): T {
    if (isEmpty && default != null) return default
    if (isEmpty && default == null) throw IllegalStateException("Attempted to access empty optional (${this})")
    return get()
}



inline fun <T, R> Iterable<T>.mapFirstNotNull(transform: (T) -> R?): R? {
    forEach { element -> transform(element)?.let { return it } }
    return null
}

inline fun <T, R> Array<T>.mapFirstNotNull(transform: (T) -> R?): R? {
    forEach { element -> transform(element)?.let { return it } }
    return null
}

inline fun <T : Any> Iterable<T?>.forEachNotNull(func: (T) -> Unit) {
    forEach { it?.let(func) }
}

inline fun <T, reified R> Array<T>.mapToArray(transform: (T) -> R) = Array(size) { i -> transform(this[i]) }
inline fun <T, reified R> List<T>.mapToArray(transform: (T) -> R) = Array(size) { i -> transform(this[i]) }

fun <T : Any> Array<T?>.castNotNull(): Array<T> {
    @Suppress("UNCHECKED_CAST")
    return this as Array<T>
}

fun Module.findChildren(): Set<Module> {
    return runReadAction {
        val manager = ModuleManager.getInstance(project)
        val result = mutableSetOf<Module>()

        for (m in manager.modules) {
            if (m === this) {
                continue
            }

            val path = manager.getModuleGroupPath(m) ?: continue
            val namedModule = path.last()?.let { manager.findModuleByName(it) } ?: continue

            if (namedModule != this) {
                continue
            }

            result.add(m)
        }

        return@runReadAction result
    }
}



inline fun <T : Any?> runWriteTask(crossinline func: () -> T): T {
    return invokeAndWait {
        ApplicationManager.getApplication().runWriteAction(Computable { func() })
    }
}

fun runWriteTaskLater(func: () -> Unit) {
    invokeLater {
        ApplicationManager.getApplication().runWriteAction(func)
    }
}

inline fun <T : Any?> Project.runWriteTaskInSmartMode(crossinline func: () -> T): T {
    if (ApplicationManager.getApplication().isReadAccessAllowed) {
        return runWriteTask { func() }
    }

    val dumbService = DumbService.getInstance(this)
    val ref = Ref<T>()
    while (true) {
        dumbService.waitForSmartMode()
        val success = runWriteTask {
            if (isDisposed) {
                throw ProcessCanceledException()
            }
            if (dumbService.isDumb) {
                return@runWriteTask false
            }
            ref.set(func())
            return@runWriteTask true
        }
        if (success) {
            break
        }
    }
    return ref.get()
}

fun <T : Any?> invokeAndWait(func: () -> T): T {
    val ref = Ref<T>()
    ApplicationManager.getApplication().invokeAndWait({ ref.set(func()) }, ModalityState.defaultModalityState())
    return ref.get()
}

fun invokeLater(func: () -> Unit) {
    ApplicationManager.getApplication().invokeLater(func, ModalityState.defaultModalityState())
}

fun invokeLater(expired: Condition<*>, func: () -> Unit) {
    ApplicationManager.getApplication().invokeLater(func, ModalityState.defaultModalityState(), expired)
}

fun invokeLaterAny(func: () -> Unit) {
    ApplicationManager.getApplication().invokeLater(func, ModalityState.any())
}

inline fun <T : Any?> PsiFile.runWriteAction(crossinline func: () -> T) =
    applyWriteAction { func() }

inline fun <T : Any?> PsiFile.applyWriteAction(crossinline func: PsiFile.() -> T): T {
    val result = WriteCommandAction.writeCommandAction(this).withGlobalUndo().compute<T, Throwable> { func() }
    PsiDocumentManager.getInstance(project)
        .doPostponedOperationsAndUnblockDocument(
            FileDocumentManager.getInstance().getDocument(this.virtualFile) ?: return result
        )
    return result
}

fun waitForAllSmart() {
    for (project in ProjectManager.getInstance().openProjects) {
        if (!project.isDisposed) {
            DumbService.getInstance(project).waitForSmartMode()
        }
    }
}



fun libraryKind(id: String): LibraryKind = LibraryKind.findById(id) ?: LibraryKind.create(id)
