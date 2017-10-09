package jetbrains.buildServer.dotnet

interface SemanticVersionParser {
    fun tryParse(version: String): SemanticVersion?
}