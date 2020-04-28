package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.AgentPropertiesProvider
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.PEReader
import jetbrains.buildServer.util.PEReader.PEUtil
import org.apache.log4j.Logger
import java.io.File
import java.util.regex.Pattern

class MSBuildFileSystemAgentPropertiesProvider(
        private val _visualStudioLocator: VisualStudioLocator,
        private val _fileSystemService: FileSystemService,
        private val _peReader: PEReader)
    : AgentPropertiesProvider {

    override val desription = "MSBuild in file system"

    override val properties get() =
        _visualStudioLocator.instances
                // C:\Program Files (x86)\Microsoft Visual Studio\2019\Professional
                .map {
                    LOG.debug("Goes through \"$it\".")
                    File(it.installationPath)
                }
                .filter { _fileSystemService.isExists(it) }
                .filter { _fileSystemService.isDirectory(it) }
                // C:\Program Files (x86)\Microsoft Visual Studio\2019\Professional\MSBuild
                .map { File(it, "MSBuild") }
                .filter {
                    LOG.debug("Goes through \"$it\".")
                    _fileSystemService.isExists(it)
                }
                .filter { _fileSystemService.isDirectory(it) }
                // dir C:\Program Files (x86)\Microsoft Visual Studio\2019\Professional\MSBuild
                .flatMap { _fileSystemService.list(it) }
                .filter {
                    LOG.debug("Goes through \"$it\".")
                    _fileSystemService.isDirectory(it)
                }
                .filter { MSBuildVersionPatter.matches(it.name) }
                // C:\Program Files (x86)\Microsoft Visual Studio\2019\Professional\MSBuild\Current\Bin
                .map { File(it, "Bin") }
                .filter {
                    LOG.debug("Goes through \"$it\".")
                    _fileSystemService.isExists(it)
                }
                .filter { _fileSystemService.isDirectory(it) }
                // C:\Program Files (x86)\Microsoft Visual Studio\2019\Professional\MSBuild\Current\Bin\MSBuild.exe
                // C:\Program Files (x86)\Microsoft Visual Studio\2019\Professional\MSBuild\Current\Bin\amd64\MSBuild.exe
                .flatMap {
                    msbuildPath -> sequence {
                        yield(MSBuildInfo(File(msbuildPath, "MSBuild.exe"), Platform.x86))
                        val amd64Directory = File(msbuildPath, "amd64")
                        if(_fileSystemService.isExists(amd64Directory) && _fileSystemService.isDirectory(amd64Directory)) {
                            yield(MSBuildInfo(File(amd64Directory, "MSBuild.exe"), Platform.x64))
                        }
                    }
                }
                .filter {
                    LOG.debug("Goes through \"${it.path}\".")
                    _fileSystemService.isExists(it.path)
                }
                .filter { _fileSystemService.isFile(it.path) }
                .mapNotNull {
                    LOG.debug("Getting a product version for \"${it.path}\".")
                    _peReader.tryGetProductVersion(it.path)?.let { version ->
                        AgentProperty("MSBuildTools${version.p1}.0_${it.platform.id}_Path", it.path.parent ?: "")
                    }
                }

    data class MSBuildInfo(val path: File, val platform: Platform)

    companion object {
        private val LOG = Logger.getLogger(MSBuildFileSystemAgentPropertiesProvider::class.java)
        private val MSBuildVersionPatter = Regex("(\\d+\\.\\d|Current)")
    }
}