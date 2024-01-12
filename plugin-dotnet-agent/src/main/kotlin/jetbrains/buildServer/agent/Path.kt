

package jetbrains.buildServer.agent

import java.io.File

data class Path(val path: String) {
    override fun toString(): String = path
}

public fun Path.extension() = File(this.path).extension