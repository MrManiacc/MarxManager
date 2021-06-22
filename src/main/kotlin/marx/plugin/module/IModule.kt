package marx.plugin.module

import javax.swing.JComponent

interface IModule {
    var group: String
    var artifact: String
    var version: String

    val content: JComponent

    /**
     * This should be called when the component is disposed, it will give the allusion of a new [JComponent]
     */
    fun reset() {
        group = "marx"
        artifact = ""
        version = "1.0.0-SNAPSHOT"
    }
}