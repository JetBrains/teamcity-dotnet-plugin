package jetbrains.buildServer.dotnet.commands.targeting

interface TargetsParser {
    fun parse(targets: String): String
}