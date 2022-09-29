package jetbrains.buildServer.dotnet.discovery.dotnetFramework

interface DotnetFrameworksProvider {
    fun getFrameworks(): Sequence<DotnetFramework>
}