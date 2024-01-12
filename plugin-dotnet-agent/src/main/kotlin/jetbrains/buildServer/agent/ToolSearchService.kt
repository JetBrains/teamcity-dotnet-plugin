

package jetbrains.buildServer.agent

import java.io.File

interface ToolSearchService {
    fun find(toolName: String, paths: Sequence<Path>): Sequence<File>
}