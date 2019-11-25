package jetbrains.buildServer.agent

import jetbrains.buildServer.util.OSType

interface Environment {
    fun tryGetVariable(name: String): String?

    val paths: Sequence<Path>

    val os: OSType
}