

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.ToolVersionOutputParser

class DotnetVersionParser : ToolVersionOutputParser {
    /**
     * Returns cleaned .net core sdk version.
     * **/
    override fun parse(output: Collection<String>): Version = output
        .map { Version.parse(it) }
        .firstOrNull { it.digits > 2 } ?: Version.Empty
}