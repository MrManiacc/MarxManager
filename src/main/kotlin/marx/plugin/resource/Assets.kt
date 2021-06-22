package marx.plugin.resource

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

/**
 * This abstract class allows for objects to directly load icons from the given path
 */
abstract class Assets protected constructor() {
    /**Provides an icon via the [path]. This is useful for static object**/
    protected fun loadIcon(path: String): Icon =
        IconLoader.getIcon(path, Assets::class.java)
}