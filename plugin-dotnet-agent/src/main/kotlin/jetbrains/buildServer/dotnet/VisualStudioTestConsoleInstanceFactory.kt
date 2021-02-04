package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.PEReader
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.agent.runner.ToolInstanceFactory
import jetbrains.buildServer.agent.Logger
import java.io.File

class VisualStudioTestConsoleInstanceFactory(
        private val _fileSystemService: FileSystemService,
        private val _peReader: PEReader)
    : ToolInstanceFactory {

    override fun tryCreate(path: File, baseVersion: Version, platform: Platform) =
        tryCreate(path, platform) ?: tryCreate(File(File(File(path, "CommonExtensions"), "Microsoft"), "TestWindow"), platform)

    private fun tryCreate(basePath: File, platform: Platform): ToolInstance? {
        if (!_fileSystemService.isExists(basePath) || !_fileSystemService.isDirectory(basePath)) {
            LOG.debug("Cannot find \"$basePath\".")
            return null
        }
        else {
            val vstestFile = File(basePath, "vstest.console.exe")
            if (!_fileSystemService.isExists(vstestFile) || !_fileSystemService.isFile(vstestFile)) {
                LOG.debug("Cannot find \"$vstestFile\".")
                return null
            }
            else {
                var detailedVersion = _peReader.tryGetVersion(vstestFile)
                if (detailedVersion == Version.Empty) {
                    LOG.warn("Cannot get a product version from \"$vstestFile\".")
                    return null
                }

                return ToolInstance(ToolInstanceType.VisualStudioTest, vstestFile, detailedVersion, Version(detailedVersion.major, detailedVersion.minor), platform)
            }
        }
    }

    companion object {
        private val LOG = Logger.getLogger(VisualStudioTestConsoleInstanceFactory::class.java)
    }
}