package marx.plugin.marxui

import com.intellij.icons.AllIcons
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Couple
import com.intellij.openapi.wm.impl.welcomeScreen.WelcomeScreenComponentFactory
import com.intellij.openapi.wm.impl.welcomeScreen.WelcomeScreenUIManager
import com.intellij.ui.AnActionButton.AnActionButtonWrapper
import com.intellij.ui.ComponentUtil
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.ui.components.panels.NonOpaquePanel
import com.intellij.ui.components.panels.Wrapper
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import marx.plugin.actions.MarxActions
import marx.plugin.resource.Localize
import marx.plugin.resource.Localize.Bundle.BUNDLE
import org.jetbrains.annotations.PropertyKey
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.util.*
import javax.swing.*

interface IGui {

    /**
     * Creates a swing label for the given text
     */
    fun label(@PropertyKey(resourceBundle = BUNDLE) key: String): JComponent {
        val label = JBLabel(Localize(key))
        label.componentStyle = UIUtil.ComponentStyle.SMALL
        label.fontColor = UIUtil.FontColor.BRIGHTER
        label.border = JBUI.Borders.empty(0, 5, 2, 0)
        return label
    }


    /**
     * Creates a swing label for the given text
     */
    fun label(@PropertyKey(resourceBundle = BUNDLE) key: String, icon: Icon): JComponent {
        val label = JBLabel(Localize(key))
        label.icon = icon
        label.componentStyle = UIUtil.ComponentStyle.SMALL
        label.fontColor = UIUtil.FontColor.BRIGHTER
        label.border = JBUI.Borders.empty(0, 5, 2, 0)
        return label
    }

    /**
     * This will create a title from the given key
     */
    fun createTitle(@PropertyKey(resourceBundle = BUNDLE) key: String): JBLabel {
        val titleLabel = JBLabel(Localize(key), SwingConstants.CENTER)
        titleLabel.isOpaque = false
        val componentFont = titleLabel.font
        titleLabel.font =
            componentFont.deriveFont(componentFont.size + JBUIScale.scale(16).toFloat()).deriveFont(Font.BOLD)
        return titleLabel
    }

    fun createCommentLabel(@PropertyKey(resourceBundle = BUNDLE) key: String): JBLabel {
        val commentFirstLabel = JBLabel(Localize(key), SwingConstants.CENTER)
        commentFirstLabel.isOpaque = false
        commentFirstLabel.foreground = UIUtil.getContextHelpForeground()
        return commentFirstLabel
    }

    fun isActionAvailable(action: AnAction): Boolean {
        val event =
            AnActionEvent.createFromAnAction(action, null, MarxActions.ACTION_CREATE_MODULE, DataContext.EMPTY_CONTEXT)
        action.update(event)
        return event.presentation.isEnabledAndVisible
    }

    fun splitActionGroupToMainAndMore(
        actionGroup: ActionGroup,
        mainButtonsNum: Int
    ): Couple<DefaultActionGroup> {
        val group = DefaultActionGroup()
        WelcomeScreenComponentFactory.collectAllActions(group, actionGroup)
        val actions = group.getChildren(null)
        val main = DefaultActionGroup()
        val more: DefaultActionGroup =
            object : DefaultActionGroup(Localize("marx.screen.empty.projects.more.text"), true) {
                override fun hideIfNoVisibleChildren(): Boolean {
                    return true
                }
            }
        for (child in actions) {
            if (!isActionAvailable(child!!)) continue
            if (main.childrenCount < mainButtonsNum) {
                main.addAction(child)
            } else {
                more.addAction(child)
            }
        }
        return Couple.of(main, more)
    }


    fun createActionListenerForComponent(component: JComponent, action: AnAction): ActionListener {
        return ActionListener { l: ActionEvent? ->
            val toolbar = ComponentUtil.getParentOfType(ActionToolbar::class.java, component)
            val dataContext =
                toolbar?.toolbarDataContext ?: DataManager.getInstance()
                    .getDataContext(component)
            action.actionPerformed(
                AnActionEvent.createFromAnAction(
                    action,
                    null,
                    MarxActions.ACTION_CREATE_MODULE,
                    dataContext
                )
            )
        }
    }

    fun createActionsToolbar(actionGroup: ActionGroup?): ActionToolbarImpl {
        val actionToolbar = ActionToolbarImpl(MarxActions.ACTION_CREATE_MODULE, actionGroup!!, true)
        actionToolbar.layoutPolicy = ActionToolbar.NOWRAP_LAYOUT_POLICY
        actionToolbar.border = JBUI.Borders.emptyTop(50)
        actionToolbar.isOpaque = false
        return actionToolbar
    }

    fun createLinkWithPopup(actionGroup: ActionGroup): LinkLabel<String?> {
        val moreLink = LinkLabel(
            actionGroup.templateText, AllIcons.General.LinkDropTriangle,
            { s: LinkLabel<String?>?, _: String? ->
                JBPopupFactory.getInstance().createActionGroupPopup(
                    null, actionGroup,
                    DataManager
                        .getInstance()
                        .getDataContext(s),
                    JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                    true
                ).showUnderneathOf(s!!)
            }, null
        )
        moreLink.horizontalTextPosition = SwingConstants.LEADING
        moreLink.border = JBUI.Borders.emptyTop(30)
        return moreLink
    }

    companion object {
        fun wrapAsBigIconWithText(action: AnAction): LargeIconWithTextWrapper = LargeIconWithTextWrapper(action)

    }

    class LargeIconWithTextWrapper(action: AnAction) :
        AnActionButtonWrapper(action.templatePresentation, action), CustomComponentAction, IGui {
        val myIconButton: JButton = JButton()
        val myLabel: JBLabel
        private val myPanel: JPanel
        fun updateIconBackground(selected: Boolean) {
            myIconButton.isSelected = selected
            myIconButton.putClientProperty(
                "JButton.backgroundColor",
                WelcomeScreenUIManager.getActionsButtonBackground(selected)
            )
            myIconButton.repaint()
        }

        override fun createCustomComponent(presentation: Presentation, place: String): JComponent = myPanel

        override fun updateButton(e: AnActionEvent) {
            delegate.update(e)
            myIconButton.icon = e.presentation.icon
            myIconButton.selectedIcon = e.presentation.selectedIcon
            myLabel.text = e.presentation.text
            UIUtil.setEnabled(myPanel, e.presentation.isEnabled, true)
        }


        init {
            myIconButton.border = JBUI.Borders.empty()
            myIconButton.horizontalAlignment = SwingConstants.CENTER
            myIconButton.isOpaque = false
            myIconButton.preferredSize = JBDimension(60, 60)
            myIconButton.putClientProperty(
                "JButton.focusedBackgroundColor",
                WelcomeScreenUIManager.getActionsButtonBackground(true)
            )
            myIconButton.putClientProperty(
                "JButton.backgroundColor",
                WelcomeScreenUIManager.getActionsButtonBackground(false)
            )
            myIconButton.addFocusListener(object : FocusListener {
                override fun focusGained(e: FocusEvent) {
                    updateIconBackground(true)
                }

                override fun focusLost(e: FocusEvent) {
                    updateIconBackground(false)
                }
            })
            myIconButton.addActionListener(
                createActionListenerForComponent(
                    myIconButton,
                    action
                )
            )
            val iconWrapper = Wrapper(myIconButton)
            iconWrapper.border = JBUI.Borders.empty(0, 30)
            myLabel = Objects.requireNonNull(templateText)?.let { JBLabel(it, SwingConstants.CENTER) }!!
            myLabel.isOpaque = false
            myPanel = NonOpaquePanel(VerticalFlowLayout(VerticalFlowLayout.TOP, 0, JBUI.scale(12), false, false))
            myPanel.add(iconWrapper)
            myPanel.add(myLabel)
        }
    }


}