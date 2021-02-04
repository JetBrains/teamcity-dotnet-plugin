package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.PEReader
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.agent.runner.ToolInstanceFactory
import jetbrains.buildServer.agent.Logger
import java.io.File

class MSTestConsoleInstanceFactory(
        private val _fileSystemService: FileSystemService,
        private val _peReader: PEReader)
    : ToolInstanceFactory {

    override fun tryCreate(path: File, baseVersion: Version, platform: Platform): ToolInstance? {
        if (!_fileSystemService.isExists(path) || !_fileSystemService.isDirectory(path)) {
            LOG.debug("Cannot find \"$path\".")
            return null
        }
        else {
            val mstestFile = File(path, "MSTest.exe")
            if (!_fileSystemService.isExists(mstestFile) || !_fileSystemService.isFile(mstestFile)) {
                LOG.debug("Cannot find \"$mstestFile\".")
                return null
            }
            else {
                var detailedVersion = _peReader.tryGetVersion(mstestFile)
                if (detailedVersion == Version.Empty) {
                    LOG.warn("Cannot get a product version from \"$mstestFile\".")
                    return null
                }

                return ToolInstance(ToolInstanceType.MSTest, mstestFile, detailedVersion, Version(detailedVersion.major, detailedVersion.minor), platform)
            }
        }
    }

    companion object {
        private val LOG = Logger.getLogger(MSTestConsoleInstanceFactory::class.java)
    }
}