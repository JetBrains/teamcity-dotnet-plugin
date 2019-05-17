package jetbrains.buildServer.agent

import java.io.File

interface ToolSearchService {
    fun find(toolName: String,
             environmentVariableName: String,
             additionalPaths: Sequence<File>,
             basePathResolver: (File) -> File = { it }): Sequence<File>
}