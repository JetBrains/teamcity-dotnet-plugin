package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.AgentPropertiesProvider
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.PEReader
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.runner.ToolInstanceProvider
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_MSBUILD_TOOLS
import jetbrains.buildServer.agent.Logger
import java.io.File

class MSBuildFileSystemAgentPropertiesProvider(
        private val _visualStudioProviders: List<ToolInstanceProvider>,
        private val _fileSystemService: FileSystemService,
        private val _peReader: PEReader)
    : AgentPropertiesProvider {

    override val desription = "MSBuild in file system"

    override val properties get() =
        _visualStudioProviders
                .asSequence()
                .flatMap { it.getInstances().asSequence() }
                .filter { it.toolType == ToolInstanceType.VisualStudio }
                // C:\Program Files (x86)\Microsoft Visual Studio\2019\Professional\Common7\IDE\
                .map {
                    LOG.debug("Goes through \"$it\".")
                    it.installationPath
                }
                .mapNotNull {
                    val base = it.parentFile?.parentFile
                    LOG.debug("Goes through \"$base\".")
                    base
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
                    _peReader.tryGetVersion(it.path).let { version ->
                        AgentProperty(ToolInstanceType.MSBuildTool, "$CONFIG_PREFIX_MSBUILD_TOOLS${version.major}.0_${it.platform.id}_Path", it.path.parent ?: "")
                    }
                }

    data class MSBuildInfo(val path: File, val platform: Platform)

    companion object {
        private val LOG = Logger.getLogger(MSBuildFileSystemAgentPropertiesProvider::class.java)
        private val MSBuildVersionPatter = Regex("(\\d+\\.\\d|Current)")
    }
}