package jetbrains.buildServer.dotnet

interface DotnetFrameworksProvider {
    val frameworks: Sequence<DotnetFramework>
}