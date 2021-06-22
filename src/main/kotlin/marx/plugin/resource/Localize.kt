package marx.plugin.resource

import com.intellij.DynamicBundle
import marx.plugin.resource.Localize.Bundle.BUNDLE
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey


/**
 * This is used for printing out anything text related. This allows for different languages`
 */
object Localize : DynamicBundle(BUNDLE) {
    /** This will get the message via the given property key
     **/
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String): String =
        getMessage(key)

    operator fun invoke(@PropertyKey(resourceBundle = BUNDLE) key: String): String = message(key)

    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String =
        getMessage(key, *params)

    operator fun invoke(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String =
        getMessage(key, *params)

    /**Allows for the icon and object to all match up,
     * meaning we don't need the [BUNDLE] outside of the [Localize]**/
    object Bundle {
        @NonNls
        const val BUNDLE = "localization.MarxDev"
    }
}
