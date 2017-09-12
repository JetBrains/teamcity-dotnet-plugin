package jetbrains.buildServer.agent

import java.io.File

interface ToolSearchService {
    fun find(
            homePathEnvironmentVariableName: String,
            targets: Sequence<String>,
            basePathResolver: (File) -> File = { it }): Sequence<File>
}