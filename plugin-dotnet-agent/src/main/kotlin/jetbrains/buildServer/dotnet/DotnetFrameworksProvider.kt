package jetbrains.buildServer.dotnet

interface DotnetFrameworksProvider {
    fun getFrameworks(): Sequence<DotnetFramework>
}