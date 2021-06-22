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

import com.intellij.facet.ui.FacetEditorTab
import marx.plugin.platform.PlatformType
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class MarxFacetEditorTab(private val configuration: MarxFacetConfig) : FacetEditorTab() {

    private lateinit var panel: JPanel

    private lateinit var bukkitEnabledCheckBox: JCheckBox
    private lateinit var bukkitAutoCheckBox: JCheckBox
    private lateinit var spigotEnabledCheckBox: JCheckBox
    private lateinit var spigotAutoCheckBox: JCheckBox
    private lateinit var paperEnabledCheckBox: JCheckBox
    private lateinit var paperAutoCheckBox: JCheckBox
    private lateinit var spongeEnabledCheckBox: JCheckBox
    private lateinit var spongeAutoCheckBox: JCheckBox
    private lateinit var forgeEnabledCheckBox: JCheckBox
    private lateinit var forgeAutoCheckBox: JCheckBox
    private lateinit var fabricEnabledCheckBox: JCheckBox
    private lateinit var fabricAutoCheckBox: JCheckBox
    private lateinit var liteloaderEnabledCheckBox: JCheckBox
    private lateinit var liteloaderAutoCheckBox: JCheckBox
    private lateinit var mcpEnabledCheckBox: JCheckBox
    private lateinit var mcpAutoCheckBox: JCheckBox
    private lateinit var mixinEnabledCheckBox: JCheckBox
    private lateinit var mixinAutoCheckBox: JCheckBox
    private lateinit var bungeecordEnabledCheckBox: JCheckBox
    private lateinit var bungeecordAutoCheckBox: JCheckBox
    private lateinit var waterfallEnabledCheckBox: JCheckBox
    private lateinit var waterfallAutoCheckBox: JCheckBox
    private lateinit var velocityEnabledCheckBox: JCheckBox
    private lateinit var velocityAutoCheckBox: JCheckBox
    private lateinit var adventureEnabledCheckBox: JCheckBox
    private lateinit var adventureAutoCheckBox: JCheckBox

    private lateinit var spongeIcon: JLabel
    private lateinit var mcpIcon: JLabel
    private lateinit var mixinIcon: JLabel

    private val enableCheckBoxArray: Array<JCheckBox> by lazy {
        arrayOf(
            bukkitEnabledCheckBox,
            spigotEnabledCheckBox,
            paperEnabledCheckBox,
            spongeEnabledCheckBox,
            forgeEnabledCheckBox,
            fabricEnabledCheckBox,
            liteloaderEnabledCheckBox,
            mcpEnabledCheckBox,
            mixinEnabledCheckBox,
            bungeecordEnabledCheckBox,
            waterfallEnabledCheckBox,
            velocityEnabledCheckBox,
            adventureEnabledCheckBox
        )
    }

    private val autoCheckBoxArray: Array<JCheckBox> by lazy {
        arrayOf(
            bukkitAutoCheckBox,
            spigotAutoCheckBox,
            paperAutoCheckBox,
            spongeAutoCheckBox,
            forgeAutoCheckBox,
            fabricAutoCheckBox,
            liteloaderAutoCheckBox,
            mcpAutoCheckBox,
            mixinAutoCheckBox,
            bungeecordAutoCheckBox,
            waterfallAutoCheckBox,
            velocityAutoCheckBox,
            adventureAutoCheckBox
        )
    }

    override fun createComponent(): JComponent {
        runOnAll { enabled, auto, platformType, _, _ ->
            auto.addActionListener { checkAuto(auto, enabled, platformType) }
        }

        bukkitEnabledCheckBox.addActionListener {
            unique(
                bukkitEnabledCheckBox,
                spigotEnabledCheckBox,
                paperEnabledCheckBox
            )
        }
        spigotEnabledCheckBox.addActionListener {
            unique(
                spigotEnabledCheckBox,
                bukkitEnabledCheckBox,
                paperEnabledCheckBox
            )
        }
        paperEnabledCheckBox.addActionListener {
            unique(
                paperEnabledCheckBox,
                bukkitEnabledCheckBox,
                spigotEnabledCheckBox
            )
        }

        bukkitAutoCheckBox.addActionListener {
            all(bukkitAutoCheckBox, spigotAutoCheckBox, paperAutoCheckBox)(
                MODULE,
                SOURCES
            )
        }
        spigotAutoCheckBox.addActionListener {
            all(spigotAutoCheckBox, bukkitAutoCheckBox, paperAutoCheckBox)(
                ROOT,
                SOURCES
            )
        }
        paperAutoCheckBox.addActionListener {
            all(paperAutoCheckBox, bukkitAutoCheckBox, spigotAutoCheckBox)(
                ROOT,
                MODULE
            )
        }

        forgeEnabledCheckBox.addActionListener { also(forgeEnabledCheckBox, mcpEnabledCheckBox) }
        fabricEnabledCheckBox.addActionListener {
            also(fabricEnabledCheckBox, mixinEnabledCheckBox, mcpEnabledCheckBox)
        }
        liteloaderEnabledCheckBox.addActionListener { also(liteloaderEnabledCheckBox, mcpEnabledCheckBox) }
        mixinEnabledCheckBox.addActionListener { also(mixinEnabledCheckBox, mcpEnabledCheckBox) }

        bungeecordEnabledCheckBox.addActionListener { unique(bungeecordEnabledCheckBox, waterfallEnabledCheckBox) }
        waterfallEnabledCheckBox.addActionListener { unique(waterfallEnabledCheckBox, bungeecordEnabledCheckBox) }

        return panel
    }

    override fun getDisplayName() = "Minecraft Module Settings"

    override fun isModified(): Boolean {
        var modified = false

        runOnAll { enabled, auto, platformType, userTypes, _ ->
            modified += auto.isSelected == platformType in userTypes
            modified += !auto.isSelected && enabled.isSelected != userTypes[platformType]
        }

        return modified
    }

    override fun reset() {
        runOnAll { enabled, auto, platformType, userTypes, autoTypes ->
            auto.isSelected = platformType !in userTypes
            enabled.isSelected = userTypes[platformType] ?: (platformType in autoTypes)

            if (auto.isSelected) {
                enabled.isEnabled = false
            }
        }
    }

    override fun apply() {
        configuration.state.userChosenTypes.clear()
        runOnAll { enabled, auto, platformType, userTypes, _ ->
            if (!auto.isSelected) {
                userTypes[platformType] = enabled.isSelected
            }
        }
    }

    private inline fun runOnAll(
        run: (JCheckBox, JCheckBox, PlatformType, MutableMap<PlatformType, Boolean>, Set<PlatformType>) -> Unit
    ) {
        val state = configuration.state
        for (i in indexes) {
            run(
                enableCheckBoxArray[i],
                autoCheckBoxArray[i],
                platformTypes[i],
                state.userChosenTypes,
                state.modulePlatforms
            )
        }
    }

    private fun unique(vararg checkBoxes: JCheckBox) {
        if (checkBoxes.size <= 1) {
            return
        }

        if (checkBoxes[0].isSelected) {
            for (i in 1 until checkBoxes.size) {
                checkBoxes[i].isSelected = false
            }
        }
    }

    private fun also(vararg checkBoxes: JCheckBox) {
        if (checkBoxes.size <= 1) {
            return
        }

        if (checkBoxes[0].isSelected) {
            for (i in 1 until checkBoxes.size) {
                checkBoxes[i].isSelected = true
            }
        }
    }

    private fun all(vararg checkBoxes: JCheckBox): Invoker {
        if (checkBoxes.size <= 1) {
            return Invoker()
        }

        for (i in 1 until checkBoxes.size) {
            checkBoxes[i].isSelected = checkBoxes[0].isSelected
        }

        return object : Invoker() {
            override fun invoke(vararg indexes: Int) {
                for (i in indexes) {
                    checkAuto(autoCheckBoxArray[i], enableCheckBoxArray[i], platformTypes[i])
                }
            }
        }
    }

    private fun checkAuto(auto: JCheckBox, enabled: JCheckBox, type: PlatformType) {
        if (auto.isSelected) {
            enabled.isEnabled = false
            enabled.isSelected = type in configuration.state.modulePlatforms
        } else {
            enabled.isEnabled = true
        }
    }

    private operator fun Boolean.plus(n: Boolean) = this || n

    // This is here so we can use vararg. Can't use parameter modifiers in function type definitions for some reason
    open class Invoker {
        open operator fun invoke(vararg indexes: Int) {}
    }

    companion object {
        private const val ROOT = 0
        private const val MODULE = ROOT + 1
        private const val SOURCES = MODULE + 1
        private const val ASSETS = SOURCES + 1


        private val platformTypes = arrayOf(
            PlatformType.MARX_ROOT,
            PlatformType.MARX_MODULE,
            PlatformType.MARX_SOURCES,
            PlatformType.MARX_ASSETS,
        )

        private val indexes = intArrayOf(
            ROOT,
            MODULE,
            SOURCES,
            ASSETS,
        )
    }
}
