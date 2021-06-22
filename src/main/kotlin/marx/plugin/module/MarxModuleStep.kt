package marx.plugin.module

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import javax.swing.JComponent

/**
 * This allows for configuration over the module
 */
class MarxModuleStep(private val context: WizardContext, private val builder: MarxModuleBuilder) : ModuleWizardStep(),
    Disposable {

    private val moduleGui = MarxModuleCreationUI()

    /**
     * This displays the contentPanel of the dialogUi, meaning it's not really a dialog in this case.
     */
    override fun getComponent(): JComponent = moduleGui.content

    /** Commits data from UI into ModuleBuilder and WizardContext  */
    override fun updateDataModel() {
        builder.group = moduleGui.group
        builder.artifact = moduleGui.artifact
        builder.version = moduleGui.version
    }

    /**
     * Usually not invoked directly, see class javadoc.
     */
    override fun dispose() = moduleGui.reset()

}
