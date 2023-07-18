package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.DotnetWorkloadProviderBase.SourceType.FILE_SYSTEM
import jetbrains.buildServer.dotnet.discovery.dotnetSdk.DotnetSdksProvider
import java.io.File
import java.io.File.separator

class FileBasedDotnetWorkloadProvider(
    private val _fileSystemService: FileSystemService,
    sdksProvider: DotnetSdksProvider,
    versionEnumerator: VersionEnumerator,
) : DotnetWorkloadProviderBase(sdksProvider, versionEnumerator) {

    override fun source(dotnetExecutable: File) = Source(
        type = FILE_SYSTEM,
        path = dotnetExecutable.parent + separator + METADATA_DIRECTORY + separator + WORKLOADS_DIRECTORY
    )

    override fun getRawWorkloadsInfo(dotnetExecutable: File): Map<Version, List<String>> {
        LOG.debug("Trying to get the list of installed .NET workloads from the file system")

        val workloadsPath = File(source(dotnetExecutable).path)
        if (!workloadsPath.isDirectoryExist()) {
            LOG.info(".NET workloads are not found in directory \"$workloadsPath\". The directory does not exist")
            return emptyMap()
        }

        return workloadsPath.listChildren()
            .filter { it.isSdkBandDirectory() }
            .map { it.installedWorkloadsDirectory() }
            .filter { it.isDirectoryExist() }
            .flatMap { it.listChildren() }
            .filter { _fileSystemService.isFile(it) }
            .map { file -> file.name to Version.parse(workloadVersionFromPath(file)) }
            .distinct()
            .groupBy({ it.second }, { it.first })
            .toMap()
    }

    private fun File.listChildren() = _fileSystemService.list(this)
    private fun File.isSdkBandDirectory() = isDirectoryExist() && Version.isValid(this.name)
    private fun File.installedWorkloadsDirectory() = File(this, INSTALLED_WORKLOADS_DIRECTORY)
    private fun File.isDirectoryExist() = _fileSystemService.isExists(this) && _fileSystemService.isDirectory(this)
    private fun workloadVersionFromPath(workloadFile: File) = workloadFile.parentFile.parentFile.name

    companion object {
        private val LOG = Logger.getLogger(FileBasedDotnetWorkloadProvider::class.java)

        private const val METADATA_DIRECTORY = "metadata"
        private const val WORKLOADS_DIRECTORY = "workloads"
        private const val INSTALLED_WORKLOADS_DIRECTORY = "InstalledWorkloads"
    }
}