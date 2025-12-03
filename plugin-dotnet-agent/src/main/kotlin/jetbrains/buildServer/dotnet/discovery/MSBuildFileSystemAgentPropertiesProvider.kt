package jetbrains.buildServer.dotnet.discovery

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ToolInstanceProvider
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_MSBUILD_TOOLS
import jetbrains.buildServer.dotnet.Platform
import jetbrains.buildServer.util.FileUtil
import jetbrains.buildServer.util.PEReader.PEVersion
import java.io.File
import java.io.RandomAccessFile
import jetbrains.buildServer.util.PEReader.PEReader as TmpPEReader

class MSBuildFileSystemAgentPropertiesProvider(
        private val _visualStudioProviders: List<ToolInstanceProvider>,
        private val _fileSystemService: FileSystemService,
        private val _peReader: PEReader) {

    val properties get() =
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
                // C:\Program Files (x86)\Microsoft Visual Studio\2019\Professional\MSBuild\Current\Bin\arm64\MSBuild.exe
                .flatMap { msbuildPath ->
                    sequence {
                        yield(MSBuildInfo(File(msbuildPath, "MSBuild.exe"), Platform.x86))
                        listOf(
                            "amd64" to Platform.x64,
                            "arm64" to Platform.ARM64
                        ).forEach { (subDir, platform) ->
                            val directory = File(msbuildPath, subDir)
                            if (_fileSystemService.isExists(directory) && _fileSystemService.isDirectory(directory)) {
                                yield(MSBuildInfo(File(directory, "MSBuild.exe"), platform))
                            }
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
                    tmpTryGetVersion(it.path).let { version ->
                        if (version == Version.Empty) {
                            LOG.warn("Got empty product version for \"${it.path}\".")
                        } else {
                            LOG.debug("Product version for \"${it.path}\" is $version.")
                        }
                        AgentProperty(ToolInstanceType.MSBuildTool, "$CONFIG_PREFIX_MSBUILD_TOOLS${version.major}.0_${it.platform.id}_Path", it.path.parent ?: "")
                    }
                }

    private fun tmpTryGetVersion(file: File) =
        tmpGetProductVersion(file)?.let {
            Version(it.p1, it.p2, it.p3, it.p4)
        } ?: Version.Empty

    private fun tmpGetProductVersion(peFile: File): PEVersion? {
        var raf: RandomAccessFile? = null
        try {
            raf = RandomAccessFile(peFile, "r")
            return TmpPEReader(raf).productVersion
        } catch (e: Exception) {
            LOG.warn("Got exception while reading product version: " + e.message, e)
            return null
        } finally {
            FileUtil.close(raf)
        }
    }

    data class MSBuildInfo(val path: File, val platform: Platform)

    companion object {
        private val LOG = Logger.getLogger(MSBuildFileSystemAgentPropertiesProvider::class.java)
        private val MSBuildVersionPatter = Regex("(\\d+\\.\\d|Current)")
    }
}