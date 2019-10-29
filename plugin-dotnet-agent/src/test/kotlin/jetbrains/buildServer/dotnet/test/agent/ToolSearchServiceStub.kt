package jetbrains.buildServer.dotnet.test.agent

import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolSearchService
import java.io.File

class ToolSearchServiceStub(private val _files: Sequence<Path>) : ToolSearchService {
    override fun find(toolName: String, paths: Sequence<Path>): Sequence<File> = _files.map { File(it.path) }
}