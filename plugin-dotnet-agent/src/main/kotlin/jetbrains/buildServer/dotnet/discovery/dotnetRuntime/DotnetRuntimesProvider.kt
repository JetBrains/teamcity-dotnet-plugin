package jetbrains.buildServer.dotnet.discovery.dotnetRuntime

interface DotnetRuntimesProvider {
    fun getRuntimes(): Sequence<DotnetRuntime>
}