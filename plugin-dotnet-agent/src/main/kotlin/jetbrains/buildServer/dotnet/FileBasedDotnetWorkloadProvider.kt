package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.Version
import java.io.File

class FileBasedDotnetWorkloadProvider(
    private val _fileSystemService: FileSystemService
) : DotnetWorkloadProvider {

    override fun getInstalledWorkloads(dotnetExecutable: File): Collection<DotnetWorkload> {
        LOG.debug("Trying to get the list of installed workloads for all .NET SDKs from the file system")

        val workloadsPath = File(File(dotnetExecutable.parent, METADATA_DIRECTORY), WORKLOADS_DIRECTORY)
        if (!workloadsPath.isDirectoryExist()) {
            LOG.debug("The directory <$workloadsPath> does not exist. Seems there are no dotnet workloads installed.")
            return emptyList()
        }

        return workloadsPath.listChildren()
            .filter { it.isSdkBandDirectory() }
            .map { it.installedWorkloadsDirectory() }
            .filter { it.isDirectoryExist() }
            .flatMap { it.listChildren() }
            .filter { _fileSystemService.isFile(it) }
            .map { DotnetWorkload(it.name, Version.parse(sdkBandFromPath(it))) }
            .sortedWith(compareBy(DotnetWorkload::name, DotnetWorkload::sdkVersion))
            .toList()
    }

    private fun File.listChildren() = _fileSystemService.list(this)
    private fun File.isSdkBandDirectory() = isDirectoryExist() && Version.isValid(this.name)
    private fun File.installedWorkloadsDirectory() = File(this, INSTALLED_WORKLOADS_DIRECTORY)
    private fun File.isDirectoryExist() = _fileSystemService.isExists(this) && _fileSystemService.isDirectory(this)
    private fun sdkBandFromPath(workloadFile: File) = workloadFile.parentFile.parentFile.name

    companion object {
        private val LOG = Logger.getLogger(FileBasedDotnetWorkloadProvider::class.java)

        private const val METADATA_DIRECTORY = "metadata"
        private const val WORKLOADS_DIRECTORY = "workloads"
        private const val INSTALLED_WORKLOADS_DIRECTORY = "InstalledWorkloads"
    }
}