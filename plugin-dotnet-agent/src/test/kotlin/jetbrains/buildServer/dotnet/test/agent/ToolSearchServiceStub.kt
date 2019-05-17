package jetbrains.buildServer.dotnet.test.agent

import jetbrains.buildServer.agent.ToolSearchService
import java.io.File

class ToolSearchServiceStub(private val _files: Sequence<File>) : ToolSearchService {
    override fun find(toolName: String,
                      environmentVariableName: String,
                      additionalPaths: Sequence<File>,
                      basePathResolver: (File) -> File): Sequence<File> = _files
}