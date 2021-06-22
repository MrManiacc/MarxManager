package marx.plugin.module

import com.intellij.ide.util.projectWizard.JavaModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.roots.ModifiableRootModel
import marx.plugin.resource.Icons
import javax.swing.Icon

/**
 * This class is used for building new modules.
 */
class
MarxModuleBuilder : JavaModuleBuilder() {
    var group: String = "marx"
        internal set
    var artifact: String = ""
        internal set
    var version: String = "1.0.0-SNAPSHOT"
        internal set

    override fun getNodeIcon(): Icon  = Icons.`code-file-32`

    override fun getGroupName(): String = "Marx"

    override fun getBuilderId(): String = "MARX_MODULE"

    /**
     * This is where the root/core of the module is setup. Here we can add sources and do all kinds of fun stuff
     */
    override fun setupRootModel(model: ModifiableRootModel) {
        // TODO: add actual content to the model here list source files
    }

    override fun getModuleType(): ModuleType<*> = MarxModuleType()

    /**
     * This creates our new rendered our module step.
     */
    override fun getCustomOptionsStep(context: WizardContext, parentDisposable: Disposable): ModuleWizardStep {
        return MarxModuleStep(context, this)
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as MarxModuleBuilder

        if (group != other.group) return false
        if (artifact != other.artifact) return false
        if (version != other.version) return false

        return true
    }

    override fun hashCode(): Int {
        var result = group.hashCode()
        result = 31 * result + artifact.hashCode()
        result = 31 * result + version.hashCode()
        return result
    }

    override fun toString(): String {
        return "MarxModule(group='$group', artifact='$artifact', version='$version')"
    }


}