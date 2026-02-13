package jetbrains.buildServer.agent

import java.io.File

/**
 * TODO: to someone who knows codebase better: please deprecate in favor of [java.nio.file.Path] or add a javadoc explaining the purpose of this class
 */
data class Path(val path: String) {
    override fun toString(): String = path
}

public fun Path.extension() = File(this.path).extension