package jetbrains.buildServer.dotnet

interface DotnetBuildContextFactory {
    fun create(command: DotnetCommand): DotnetBuildContext
}