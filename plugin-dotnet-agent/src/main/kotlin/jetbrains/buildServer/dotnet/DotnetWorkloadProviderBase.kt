package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.discovery.dotnetSdk.DotnetSdksProvider
import java.io.File

abstract class DotnetWorkloadProviderBase(
    private val _sdksProvider: DotnetSdksProvider,
    private val _versionEnumerator: VersionEnumerator,
) : DotnetWorkloadProvider {

    /**
     * Represents a source where workloads are searched for (e.g. could be a root path in file system or registry)
     */
    protected abstract fun source(dotnetExecutable: File): Source

    /**
     * Provides workloads info in a "raw" format as it was presented in the source (e.g. file system or registry).
     *
     * For example, the versions here are not mapped to the actual .NET SDK versions. They are "rounded" and represent
     * .NET SDK feature bands. For example, if 6.0.407 and 6.0.408 .NET SDKs are installed, this method can
     * return only 6.0.400 if workloads have been found.
     *
     * @param dotnetExecutable dotnet executable file
     * @return a map from "rounded" .NET SDK version representing particular feature band to a list of workload names
     */
    protected abstract fun getRawWorkloadsInfo(dotnetExecutable: File): Map<Version, List<String>>

    override fun getInstalledWorkloads(dotnetExecutable: File): Collection<DotnetWorkload> {
        val workloadsInfo = getRawWorkloadsInfo(dotnetExecutable)

        if (workloadsInfo.isEmpty())
            return emptyList()

        val sdkVersions = _versionEnumerator.enumerate(_sdksProvider.getSdks(dotnetExecutable))
            .map { Version.parse(it.first) to it.second.version }
            .toList()

        val workloads = workloadsInfo
            .flatMap { workloadInfo ->
                mapToSdkVersions(workloadInfo.key, sdkVersions)
                    .flatMap { version -> workloadInfo.value.map { DotnetWorkload(it, version) } }
            }
            .distinct()
            .sortedWith(compareBy(DotnetWorkload::name, DotnetWorkload::sdkVersion))

        logSdksWithNotFoundWorkloads(dotnetExecutable, sdkVersions, workloads)

        return workloads
    }

    private fun mapToSdkVersions(
        workloadSdkFeatureBand: Version,
        sdkVersions: List<Pair<Version, Version>>
    ): List<Version> {
        return sdkVersions
            .filter { version -> isTheSameSdkFeatureBand(version.second, workloadSdkFeatureBand) }
            .map { version -> version.first }
            .ifEmpty { listOf(workloadSdkFeatureBand) }
    }

    private fun isTheSameSdkFeatureBand(version: Version, workloadVersion: Version) =
        version.toString().startsWith(workloadVersion.toString()) || (
                version.major == workloadVersion.major &&
                        version.minor == workloadVersion.minor &&
                        version.release == workloadVersion.release &&
                        version.patch / 100 == workloadVersion.patch / 100
                )

    private fun logSdksWithNotFoundWorkloads(
        dotnetExecutable: File,
        sdkVersions: List<Pair<Version, Version>>,
        workloads: List<DotnetWorkload>
    ) {
        val source = source(dotnetExecutable)
        val versionsWithWorkloads = workloads.map { it.sdkVersion }.toSet()

        sdkVersions.forEach { sdkVersion ->
            if (!versionsWithWorkloads.contains(sdkVersion.first) && sdkVersion.first == sdkVersion.second) {
                LOG.info(
                    ".NET workloads are not found for .NET SDK ${sdkVersion.first} " +
                            "in the ${source.type.description} by following path \"${source.path}\""
                )
            }
        }
    }

    protected enum class SourceType(val description: String) {
        FILE_SYSTEM("file system"),
        WINDOWS_REGISTRY("Windows Registry")
    }

    protected data class Source(val type: SourceType, val path: String)

    companion object {
        private val LOG = Logger.getLogger(FileBasedDotnetWorkloadProvider::class.java)
    }
}