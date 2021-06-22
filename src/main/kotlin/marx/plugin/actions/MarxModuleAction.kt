package marx.plugin.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.CheckoutProvider
import marx.plugin.resource.Icons
import marx.plugin.resource.Localize

class MarxModuleAction : DumbAwareAction() {
    override fun update(e: AnActionEvent) {
        val isEnabled = CheckoutProvider.EXTENSION_POINT_NAME.hasAnyExtensions()
        val presentation = e.presentation
        presentation.isEnabledAndVisible = isEnabled
        if (!isEnabled)
            return
        presentation.icon = Icons.`code-file-64`
        presentation.selectedIcon = AllIcons.General.Modified
        presentation.text = Localize("marx.create.module")
    }

    override fun actionPerformed(e: AnActionEvent) {
//        val project = e.getData(CommonDataKeys.PROJECT) ?: ProjectManager.getInstance().defaultProject
        ActionNewModule.CreateModuleUI().invoke { group, artifact, version ->
            Messages.showMessageDialog(
                "Create module: $group:$artifact:$version",
                "Created Module",
                Icons.`code-file-64`
            )
        }
    }
}

