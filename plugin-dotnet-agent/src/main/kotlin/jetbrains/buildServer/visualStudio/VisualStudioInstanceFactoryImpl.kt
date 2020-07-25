package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.PEReader
import jetbrains.buildServer.agent.Version
import org.apache.log4j.Logger
import java.io.File

class VisualStudioInstanceFactoryImpl(
        private val _fileSystemService: FileSystemService,
        private val _peReader: PEReader)
    : VisualStudioInstanceFactory {

    override fun tryCreate(path: File, version: Version): VisualStudioInstance? {
        if (!_fileSystemService.isExists(path) || !_fileSystemService.isDirectory(path)) {
            LOG.warn("Cannot find \"$path\".")
            return null
        }
        else {
            val devenvFile = File(path, "devenv.exe")
            if (!_fileSystemService.isExists(devenvFile) || !_fileSystemService.isFile(devenvFile)) {
                LOG.warn("Cannot find \"$devenvFile\".")
                return null
            }
            else {
                var fileVersion = _peReader.tryGetProductVersion(devenvFile)
                var displayVersion = version
                if (fileVersion == Version.Empty) {
                    LOG.warn("Cannot get a product version from \"$devenvFile\".")
                }
                else {
                    displayVersion = fileVersion
                }

                var productLineVersionVersion = ProductLineVersionVersions.get(Version(displayVersion.major)) ?: Version.Empty
                if (productLineVersionVersion == Version.Empty) {
                    LOG.warn("Cannot find a product line version for $version.")
                    return null
                }

                return VisualStudioInstance(path, displayVersion, productLineVersionVersion)
            }
        }
    }

    companion object {
        private val LOG = Logger.getLogger(VisualStudioInstanceFactoryImpl::class.java)

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