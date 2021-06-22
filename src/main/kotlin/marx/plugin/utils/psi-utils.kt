package marx.plugin.utils

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.impl.OrderEntryUtil
import com.intellij.psi.*
import com.intellij.psi.filters.ElementFilter
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.TypeConversionUtil
import com.intellij.refactoring.changeSignature.ChangeSignatureUtil
import com.siyeh.ig.psiutils.ImportUtils
import marx.plugin.module.modules.RootModule
import marx.plugin.module.modules.RootModuleType
import marx.plugin.platform.facet.MarxFacet
import java.util.stream.Stream


fun PsiElement.findModule(): Module? = ModuleUtilCore.findModuleForPsiElement(this)

fun PsiElement.findContainingClass(): PsiClass? = findParent(resolveReferences = false)

fun PsiElement.findReferencedClass(): PsiClass? = findParent(resolveReferences = true)

fun PsiElement.findReferencedMember(): PsiMember? = findParent({ it is PsiClass }, resolveReferences = true)

fun PsiElement.findContainingMember(): PsiMember? = findParent({ it is PsiClass }, resolveReferences = false)

fun PsiElement.findContainingMethod(): PsiMethod? = findParent({ it is PsiClass }, resolveReferences = false)

private val PsiElement.ancestors: Sequence<PsiElement>
    get() = generateSequence(this) { if (it is PsiFile) null else it.parent }

fun PsiElement.isAncestorOf(child: PsiElement): Boolean = child.ancestors.contains(this)

private inline fun <reified T : PsiElement> PsiElement.findParent(resolveReferences: Boolean): T? {
    return findParent({ false }, resolveReferences)
}

private inline fun <reified T : PsiElement> PsiElement.findParent(
    stop: (PsiElement) -> Boolean,
    resolveReferences: Boolean
): T? {
    var el: PsiElement = this

    while (true) {
        if (resolveReferences && el is PsiReference) {
            el = el.resolve() ?: return null
        }

        if (el is T) {
            return el
        }

        if (el is PsiFile || el is PsiDirectory || stop(el)) {
            return null
        }

        el = el.parent ?: return null
    }
}

// Children
fun PsiClass.findFirstMember(): PsiMember? = findChild()

fun PsiElement.findNextMember(): PsiMember? = findSibling(true)

private inline fun <reified T : PsiElement> PsiElement.findChild(): T? {
    return firstChild?.findSibling(strict = false)
}

private inline fun <reified T : PsiElement> PsiElement.findSibling(strict: Boolean): T? {
    var sibling = if (strict) nextSibling ?: return null else this
    while (true) {
        if (sibling is T) {
            return sibling
        }

        sibling = sibling.nextSibling ?: return null
    }
}

fun PsiElement.findKeyword(name: String): PsiKeyword? {
    forEachChild {
        if (it is PsiKeyword && it.text == name) {
            return it
        }
    }
    return null
}

private inline fun PsiElement.forEachChild(func: (PsiElement) -> Unit) {
    firstChild?.forEachSibling(func, strict = false)
}

private inline fun PsiElement.forEachSibling(func: (PsiElement) -> Unit, strict: Boolean) {
    var sibling = if (strict) nextSibling ?: return else this
    while (true) {
        func(sibling)
        sibling = sibling.nextSibling ?: return
    }
}

inline fun PsiElement.findLastChild(condition: (PsiElement) -> Boolean): PsiElement? {
    var child = firstChild ?: return null
    var lastChild: PsiElement? = null

    while (true) {
        if (condition(child)) {
            lastChild = child
        }

        child = child.nextSibling ?: return lastChild
    }
}

inline fun <reified T : PsiElement> PsiElement.childrenOfType(): Collection<T> =
    PsiTreeUtil.findChildrenOfType(this, T::class.java)

fun <T : Any> Sequence<T>.filter(filter: ElementFilter?, context: PsiElement): Sequence<T> {
    filter ?: return this
    return filter { filter.isAcceptable(it, context) }
}

fun Stream<out PsiElement>.toResolveResults(): Array<ResolveResult> = map(::PsiElementResolveResult).toTypedArray()

fun PsiParameterList.synchronize(newParams: List<PsiParameter>) {
    ChangeSignatureUtil.synchronizeList(this, newParams, { it.parameters.asList() }, BooleanArray(newParams.size))
}

val PsiElement.constantValue: Any?
    get() = JavaPsiFacade.getInstance(project).constantEvaluationHelper.computeConstantExpression(this)

val PsiElement.constantStringValue: String?
    get() = constantValue as? String

private val ACCESS_MODIFIERS =
    listOf(PsiModifier.PUBLIC, PsiModifier.PROTECTED, PsiModifier.PRIVATE, PsiModifier.PACKAGE_LOCAL)

fun isAccessModifier(@PsiModifier.ModifierConstant modifier: String): Boolean {
    return modifier in ACCESS_MODIFIERS
}

infix fun PsiElement.equivalentTo(other: PsiElement): Boolean {
    return manager.areElementsEquivalent(this, other)
}

fun PsiType?.isErasureEquivalentTo(other: PsiType?): Boolean {
    // TODO: Do more checks for generics instead
    return TypeConversionUtil.erasure(this) == TypeConversionUtil.erasure(other)
}

val PsiMethod.nameAndParameterTypes: String
    get() = "$name(${parameterList.parameters.joinToString(", ") { it.type.presentableText }})"

val <T : PsiElement> T.manipulator: ElementManipulator<T>?
    get() = ElementManipulators.getManipulator(this)

inline fun <T> PsiElement.cached(crossinline compute: () -> T): T {
    return CachedValuesManager.getCachedValue(this) { CachedValueProvider.Result.create(compute(), this) }
}

fun LookupElementBuilder.withImportInsertion(toImport: List<PsiClass>): LookupElementBuilder =
    this.withInsertHandler { insertionContext, _ ->
        toImport.forEach { ImportUtils.addImportIfNeeded(it, insertionContext.file) }
    }

fun PsiElement.findRootModule() = this.cached {
    val file = containingFile?.virtualFile ?: return@cached null
    val index = ProjectFileIndex.getInstance(project)
    val modules = if (index.isInLibrary(file)) {
        val library = index.getOrderEntriesForFile(file)
            .asSequence()
            .mapNotNull { it as? LibraryOrderEntry }
            .firstOrNull()
            ?.library
            ?: return@cached null
        ModuleManager.getInstance(project).modules.asSequence()
            .filter { OrderEntryUtil.findLibraryOrderEntry(ModuleRootManager.getInstance(it), library) != null }
    } else sequenceOf(this.findModule())

    modules.mapNotNull { it?.findRootModule() }.firstOrNull()
}

private fun Module.findRootModule(): RootModule? {
    var result: RootModule? = null
    ModuleUtilCore.visitMeAndDependentModules(this) {
        result = MarxFacet(it, RootModuleType())
        result == null
    }
    return result
}

