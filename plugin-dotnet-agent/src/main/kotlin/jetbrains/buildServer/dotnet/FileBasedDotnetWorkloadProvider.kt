package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.discovery.dotnetSdk.DotnetSdksProvider
import java.io.File

class FileBasedDotnetWorkloadProvider(
    private val _fileSystemService: FileSystemService,
    private val _sdksProvider: DotnetSdksProvider,
    private val _versionEnumerator: VersionEnumerator,
) : DotnetWorkloadProvider {

    override fun getInstalledWorkloads(dotnetExecutable: File): Collection<DotnetWorkload> {
        LOG.debug("Trying to get the list of installed .NET workloads from the file system")

        val workloadsPath = File(File(dotnetExecutable.parent, METADATA_DIRECTORY), WORKLOADS_DIRECTORY)
        if (!workloadsPath.isDirectoryExist()) {
            LOG.info(".NET workloads are not found in directory \"$workloadsPath\". The directory does not exist")
            return emptyList()
        }

        val sdkVersions = _versionEnumerator.enumerate(_sdksProvider.getSdks(dotnetExecutable))
            .map { Version.parse(it.first) to it.second.version }
            .toList()

        val workloads = workloadsPath.listChildren()
            .filter { it.isSdkBandDirectory() }
            .map { it.installedWorkloadsDirectory() }
            .filter { it.isDirectoryExist() }
            .flatMap { it.listChildren() }
            .filter { _fileSystemService.isFile(it) }
            .flatMap { file ->
                mapToSdkVersions(Version.parse(workloadSdkFeatureBandFromPath(file)), sdkVersions)
                    .map { DotnetWorkload(file.name, it) }
            }
            .sortedWith(compareBy(DotnetWorkload::name, DotnetWorkload::sdkVersion))
            .toList()

        logSdksWithNotFoundWorkloads(workloadsPath, sdkVersions, workloads)

        return workloads
    }

    private fun File.listChildren() = _fileSystemService.list(this)
    private fun File.isSdkBandDirectory() = isDirectoryExist() && Version.isValid(this.name)
    private fun File.installedWorkloadsDirectory() = File(this, INSTALLED_WORKLOADS_DIRECTORY)
    private fun File.isDirectoryExist() = _fileSystemService.isExists(this) && _fileSystemService.isDirectory(this)
    private fun workloadSdkFeatureBandFromPath(workloadFile: File) = workloadFile.parentFile.parentFile.name

    private fun mapToSdkVersions(
        workloadSdkFeatureBand: Version,
        sdkVersions: List<Pair<Version, Version>>
    ): List<Version> {
        return sdkVersions
            .filter { version -> isTheSameSdkFeatureBand(version.second, workloadSdkFeatureBand) }
            .map { version -> version.first }
            .ifEmpty { listOf(workloadSdkFeatureBand) }
    }

    private fun isTheSameSdkFeatureBand(version: Version, workloadSdkFeatureBand: Version) =
        version.toString().startsWith(workloadSdkFeatureBand.toString()) || (
                version.major == workloadSdkFeatureBand.major &&
                        version.minor == workloadSdkFeatureBand.minor &&
                        version.release == workloadSdkFeatureBand.release &&
                        version.patch / 100 == workloadSdkFeatureBand.patch / 100
                )

    private fun logSdksWithNotFoundWorkloads(
        workloadsPath: File,
        sdkVersions: List<Pair<Version, Version>>,
        workloads: List<DotnetWorkload>
    ) {
        val versionsWithWorkloads = workloads.map { it.sdkVersion }.toSet()

        sdkVersions.forEach { sdkVersion ->
            if (!versionsWithWorkloads.contains(sdkVersion.first)) {
                LOG.info(".NET workloads are not found for .NET SDK ${sdkVersion.first} in directory \"$workloadsPath\"")
            }
        }
    }

    companion object {
        private val LOG = Logger.getLogger(FileBasedDotnetWorkloadProvider::class.java)

        private const val METADATA_DIRECTORY = "metadata"
        private const val WORKLOADS_DIRECTORY = "workloads"
        private const val INSTALLED_WORKLOADS_DIRECTORY = "InstalledWorkloads"
    }
}