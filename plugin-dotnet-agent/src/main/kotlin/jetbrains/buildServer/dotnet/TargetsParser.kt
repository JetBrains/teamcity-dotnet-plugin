package jetbrains.buildServer.dotnet

interface TargetsParser {
    fun parse(targets: String): String
}