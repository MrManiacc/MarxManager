package marx.plugin.module

import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.openapi.wm.impl.welcomeScreen.WelcomeScreenUIManager
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.panels.NonOpaquePanel
import com.intellij.uiDesigner.core.AbstractLayout
import com.intellij.util.ui.GridBag
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import marx.plugin.marxui.IGui
import marx.plugin.resource.Icons
import java.awt.Insets
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * This class handles the creation of modules for marx in relation to guis and jcomponentsl
 */
@Suppress("DialogTitleCapitalization")
class MarxModuleCreationUI() : JPanel(), IModule, IGui {
    private val groupField = JTextField()
    override var group: String
        get() = groupField.text ?: "marx"
        set(value) = value.let { groupField.text = it }
    private val artifactField = JTextField()
    override var artifact: String
        get() = artifactField.text ?: "unnamed"
        set(value) = value.let { artifactField.text = it }
    private val versionField = JTextField("1.0.0-SNAPSHOT")
    override var version: String
        get() = versionField.text ?: "1.0.0-SNAPSHOT"
        set(value) = value.let { versionField.text = it }
    override val content: JComponent
        get() = this


    init {
        background = WelcomeScreenUIManager.getMainAssociatedComponentBackground()
        val mainPanel: JPanel = NonOpaquePanel(VerticalFlowLayout())
        mainPanel.border = JBUI.Borders.emptyTop(5)
        mainPanel.add(createTitle("marx.create.project.title"))
        val layout = GridBag()
            .setDefaultInsets(Insets(0, 0, AbstractLayout.DEFAULT_VGAP, AbstractLayout.DEFAULT_HGAP))
            .setDefaultWeightX(1.0)
        val header = JBLabel(Icons.`code-file-256`)
        header.componentStyle = UIUtil.ComponentStyle.LARGE
        mainPanel.add(header, layout.nextLine().next())
        mainPanel.add(label("marx.framework.module.group"), layout.nextLine().next().weightx(0.2))
        mainPanel.add(groupField, layout.nextLine().next().weightx(2.5))
        mainPanel.add(label("marx.framework.module.artifact"), layout.nextLine().next().weightx(0.2))
        mainPanel.add(artifactField, layout.nextLine().next().weightx(0.8))
        mainPanel.add(label("marx.framework.module.version"), layout.nextLine().next().weightx(0.2))
        mainPanel.add(versionField, layout.nextLine().next().weightx(0.8))
        add(mainPanel)
    }

    companion object {
        private const val PRIMARY_BUTTON_NUM = 3
    }
}