package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.PEReader
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.agent.runner.ToolInstanceFactory
import jetbrains.buildServer.dotnet.Platform
import jetbrains.buildServer.agent.Logger
import java.io.File

class VisualStudioInstanceFactory(
        private val _fileSystemService: FileSystemService,
        private val _peReader: PEReader)
    : ToolInstanceFactory {

    override fun tryCreate(path: File, baseVersion: Version, platform: Platform): ToolInstance? {
        if (!_fileSystemService.isExists(path) || !_fileSystemService.isDirectory(path)) {
            LOG.debug("Cannot find \"$path\".")
            return null
        }
        else {
            val devenvFile = File(path, "devenv.exe")
            if (!_fileSystemService.isExists(devenvFile) || !_fileSystemService.isFile(devenvFile)) {
                LOG.debug("Cannot find \"$devenvFile\".")
                return null
            }
            else {
                var curBaseVersion: Version
                var detailedVersion = _peReader.tryGetVersion(devenvFile)
                if (detailedVersion == Version.Empty) {
                    LOG.warn("Cannot get a product version from \"$devenvFile\".")
                    detailedVersion = baseVersion
                }

                curBaseVersion =
                        ProductLineVersionVersions.get(Version(baseVersion.major))
                        ?: ProductLineVersionVersions.get(Version(detailedVersion.major))
                        ?: Version.Empty

                if (curBaseVersion == Version.Empty) {
                    LOG.warn("Cannot find a product line version for $baseVersion.")
                    return null
                }

                return ToolInstance(ToolInstanceType.VisualStudio, path, detailedVersion, curBaseVersion, platform)
            }
        }
    }

    companion object {
        private val LOG = Logger.getLogger(VisualStudioInstanceFactory::class.java)

        private val ProductLineVersionVersions = mapOf(
                Version(15) to Version.parse("2017"),
                Version(14) to Version.parse("2015"),
                Version(12) to Version.parse("2013"),
                Version(11) to Version.parse("2012"),
                Version(10) to Version.parse("2010"),
                Version(9) to Version.parse("2008"),
                Version(8) to Version.parse("2005"),
                Version(7) to Version.parse("2003")
        )
    }
}