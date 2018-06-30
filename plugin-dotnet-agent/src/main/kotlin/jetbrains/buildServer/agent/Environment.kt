package jetbrains.buildServer.agent

import jetbrains.buildServer.util.OSType
import java.io.File

interface Environment {
    fun tryGetVariable(name: String): String?

    val paths: Sequence<File>

    val os: OSType
}