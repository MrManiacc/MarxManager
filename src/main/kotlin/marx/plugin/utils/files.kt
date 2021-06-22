/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package marx.plugin.utils

import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.*
import com.intellij.util.io.URLUtil
import org.jetbrains.annotations.Contract
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.util.jar.Attributes
import java.util.jar.JarFile
import java.util.jar.Manifest

/**
 * Pattern:
 *      ^.*?/(assets|data)/([^/]+)/(.*?)$
 * 1st Capturing Group:
 *      (assets|data)
 *
 * 1st Alternative assets:
 *      assets matches the characters assets literally (case sensitive)
 *
 * 2nd Alternative data:
 *      data matches the characters data literally (case sensitive)
 *
 * 2nd Capturing Group ([^/]+):
 *      Match a single character not present in the list below [^/]
 *      + matches the previous token between one and unlimited times, as many times as possible, giving back as needed (greedy)
 *      / matches the character / literally (case sensitive)
 *      / matches the character / literally (case sensitive)
 * 3rd Capturing Group (.*?)
 *      . matches any character (except for line terminators)
 *      $ asserts position at the end of a line
 */
private val ResourceLocationPattern = Regex("^.*?/(assets|data)/([^/]+)/(.*?)$")


val VirtualFile.localFile: File
    get() = VfsUtilCore.virtualToIoFile(this)

/**
 * This attempts to retrieve the jar [Manifest], returning null if not found via an [IOException]
 */
val VirtualFile.manifest: Manifest?
    get() = try {
        JarFile(localFile).use { it.manifest }
    } catch (e: IOException) {
        null
    }

/**
 * This will get the corresponding domain of the given VirtualFile.
 */
val VirtualFile.marxDomain: String?
    get() = ResourceLocationPattern.matchEntire(this.path)?.groupValues?.get(2)

/**
 * This will get the corresponding marx related path of the given [VirtualFile].
 */
val VirtualFile.marxPath: String?
    get() = ResourceLocationPattern.matchEntire(this.path)?.groupValues?.get(3)

/**
 * Refreshes the filesystem, meaning it will update the source for the given [VirtualFile].
 */
fun VirtualFile.refreshFs(): VirtualFile = this.parent.findOrCreateChildData(this, this.name)

/**
 * This will retrieve the jar manifest value for the given attribute name
 */
operator fun Manifest.get(attribute: String): String? = mainAttributes.getValue(attribute)

/**
 * This will retrieve the jar manifest value(s) for the given attribute name via an [Attributes] map
 */
operator fun Manifest.get(attribute: Attributes.Name): String? = mainAttributes.getValue(attribute)

/**
 * This will retrieve the virtual file, refreshing the local file system firstl.
 */
val Path.virtualFile: VirtualFile?
    get() = LocalFileSystem.getInstance().refreshAndFindFileByPath(this.toAbsolutePath().toString())

/**
 * This will get the virtual file from local [Path] or throws an [IllegalStateException]
 */
val Path.virtualFileOrError: VirtualFile
    get() = virtualFile ?: throw IllegalStateException("Failed to find file: $this")

const val EXT_JAR = ".jar"

const val EXT_ZIP = ".zip"

/**
 * Converts the given path to an URL. The underlying implementation "cheats": it doesn't encode spaces and it just adds the "file"
 * protocol at the beginning of this path. We use this method when creating URLs for file paths that will be included in a module's
 * content root, because converting a URL back to a path expects the path to be constructed the way this method does. To obtain a real
 * URL from a file path, use [com.android.utils.SdkUtils.fileToUrl].
 *
 * @param path the given path.
 * @return the created URL.
 */
fun pathToIdeaUrl(path: File): String {
    val name = path.name
    val isJarFile = FileUtilRt.extensionEquals(name, EXT_JAR) || FileUtilRt.extensionEquals(name, EXT_ZIP)
    // .jar files require an URL with "jar" protocol.
    val protocol = if (isJarFile) StandardFileSystems.JAR_PROTOCOL else StandardFileSystems.FILE_PROTOCOL
    var url = VirtualFileManager.constructUrl(protocol, FileUtil.toSystemIndependentName(path.path))
    if (isJarFile) {
        url += URLUtil.JAR_SEPARATOR
    }
    return url
}

fun getJarFromJarUrl(url: String): File? {
    // URLs for jar file start with "jar://" and end with "!/".
    if (!url.startsWith(StandardFileSystems.JAR_PROTOCOL_PREFIX)) {
        return null
    }
    var path = url.substring(StandardFileSystems.JAR_PROTOCOL_PREFIX.length)
    val index = path.lastIndexOf(URLUtil.JAR_SEPARATOR)
    if (index != -1) {
        path = path.substring(0, index)
    }
    return toSystemDependentPath(path)
}

/**
 * Converts the given `String` path to a system-dependent path (as [File].)
 */
@Contract("!null -> !null")
fun toSystemDependentPath(path: String?): File? {
    return if (path != null) File(FileUtilRt.toSystemDependentName(path)) else null
}