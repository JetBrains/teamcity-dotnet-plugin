package jetbrains.buildServer.dotnet

import java.io.File

interface DotnetRuntimesProvider {
    fun getRuntimes(): Sequence<DotnetRuntime>
}