package jetbrains.buildServer.agent.runner

import java.io.File

interface CacheCleaner {
    val name: String

    val type: CleanType

    val targets: Sequence<File>

    fun clean(target: File): Boolean
}