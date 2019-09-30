package jetbrains.buildServer.agent

import java.io.File

data class Path(val path: String)

public fun Path.extension() = File(this.path).extension