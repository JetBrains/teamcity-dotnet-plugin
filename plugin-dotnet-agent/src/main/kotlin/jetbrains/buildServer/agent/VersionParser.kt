package jetbrains.buildServer.agent

import jetbrains.buildServer.dotnet.Version

interface VersionParser {
    fun parse(output: Collection<String>): Version
}