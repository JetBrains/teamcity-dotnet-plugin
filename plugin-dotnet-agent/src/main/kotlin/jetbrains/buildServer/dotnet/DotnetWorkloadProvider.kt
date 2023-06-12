package jetbrains.buildServer.dotnet

import java.io.File

interface DotnetWorkloadProvider {

    /**
     * Provides a collection of installed workloads for all detected .NET SDK.
     *
     * @param dotnetExecutable represents a dotnet executable file.
     * @return a collection of installed workloads for all detected .NET SDK.
     */
    fun getInstalledWorkloads(dotnetExecutable: File): Collection<DotnetWorkload>
}