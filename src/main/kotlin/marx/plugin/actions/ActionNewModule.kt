package marx.plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBLabel
import com.intellij.uiDesigner.core.AbstractLayout
import com.intellij.util.ui.GridBag
import com.intellij.util.ui.UIUtil
import com.sun.java.accessibility.util.AWTEventMonitor
import marx.plugin.marxui.IGui
import marx.plugin.resource.Icons
import marx.plugin.state.MarxManConfig
import marx.plugin.utils.ModuleData
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.KeyStroke

/**
 * This will add a module. It takes input dialog.
 */
class ActionNewModule : AnAction() {

    /**
     * Implement this method to provide your action handler.
     *
     * @param e Carries information on the invocation place
     */
    override fun actionPerformed(e: AnActionEvent) {
        CreateModuleUI().invoke { group, artifact, version ->
            Messages.showMessageDialog(
                "Create module: $group:$artifact:$version",
                "Created Module",
                Icons.`code-file-64`
            )
        }
    }


    /**
     * This form allows for creation of modules
     */
    class CreateModuleUI : DialogWrapper(true), IGui {
        internal val panel = JPanel(GridBagLayout())
        internal val groupField = JTextField()
        internal val artifactField = JTextField()
        internal val versionField = JTextField("1.0.0-SNAPSHOT")

        init {
            init()
            title = "Marx Module"
            AWTEventMonitor.addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent) {
                    onCancel()
                }
            })
            //call onCancel() on ESCAPE
            panel.registerKeyboardAction(
                { onCancel() },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
            )
            groupField.text = MarxManConfig().state.savedGroup
        }


        /**
         * Factory method. It creates panel with dialog options. Options panel is located at the
         * center of the dialog's content pane. The implementation can return `null`
         * value. In this case there will be no options panel.
         */
        override fun createCenterPanel(): JComponent {
            val layout = GridBag()
                .setDefaultInsets(Insets(0, 0, AbstractLayout.DEFAULT_VGAP, AbstractLayout.DEFAULT_HGAP))
                .setDefaultWeightX(2.0)
                .setDefaultFill(GridBagConstraints.HORIZONTAL)
            panel.preferredSize = Dimension(540, 330)
            val header = JBLabel(Icons.`code-file-128`)
            header.componentStyle = UIUtil.ComponentStyle.LARGE
            panel.add(header, layout.nextLine().next())
            panel.add(label("marx.framework.module.group"), layout.nextLine().next().weightx(0.2))
            panel.add(groupField, layout.nextLine().next().weightx(0.6))
            panel.add(label("marx.framework.module.artifact"), layout.nextLine().next().weightx(0.2))
            panel.add(artifactField, layout.nextLine().next().weightx(0.8))
            panel.add(label("marx.framework.module.version"), layout.nextLine().next().weightx(0.2))
            panel.add(versionField, layout.nextLine().next().weightx(0.8))
            return panel
        }


        /**
         * This will create and invoke the callback
         */
        operator fun invoke(onCreate: ModuleData) {
            if (showAndGet()) {
                MarxManConfig().state.savedGroup = groupField.text
                onCreate(this.groupField.text, this.artifactField.text, this.versionField.text)
                dispose()
            }
        }

        /**
         * Called upon canceling the action
         */
        private fun onCancel() {
            val result = Messages.showOkCancelDialog(
                "Are you sure you'd like to cancel the creation of the module?",
                "Are You Sure?",
                "Yes",
                "No",
                Messages.getQuestionIcon()
            )
            if (result == 0)
                dispose()
        }
    }


}
