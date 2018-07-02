package jetbrains.buildServer.dotnet

interface CommandRegistry {
    fun register(dotnetCommandType: DotnetCommandType)
}