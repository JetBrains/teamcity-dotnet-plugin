package jetbrains.buildServer.dotnet

interface DotnetFilterFactory {
    fun createFilter(command: DotnetCommandType): DotnetFilter
}