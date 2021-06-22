package marx.plugin.state

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "MarxMan",
    storages = [Storage("marx-man.xml")]
)
class MarxManConfig : PersistentStateComponent<MarxManState> {
    /**
     * This is our internal state. It is dynamically updated/serialized via intellij magic
     */
    var marxState = MarxManState()
        private set

    /**
     * @return a component state. All properties, public and annotated fields are serialized. Only values, which differ
     * from the default (i.e., the value of newly instantiated class) are serialized. `null` value indicates
     * that the returned state won't be stored, as a result previously stored state will be used.
     * @see com.intellij.util.xmlb.XmlSerializer
     */
    override fun getState(): MarxManState = marxState

    /**
     * This method is called when new component state is loaded. The method can and will be called several times, if
     * config files were externally changed while IDE was running.
     *
     *
     * State object should be used directly, defensive copying is not required.
     *
     * @param state loaded component state
     * @see com.intellij.util.xmlb.XmlSerializerUtil.copyBean
     */
    override fun loadState(state: MarxManState) = state.let { this.marxState = it }

    companion object {

        /**
         * This is used to retrieve the [MarxManConfig] instance
         */
        @JvmStatic
        operator fun invoke(): PersistentStateComponent<MarxManState> =
            ServiceManager.getService(MarxManConfig::class.java)
    }

}