package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.PEReader
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.agent.runner.ToolInstanceFactory
import jetbrains.buildServer.agent.Logger
import java.io.File

class DotnetFrameworkSdkInstanceFactory(
        private val _fileSystemService: FileSystemService,
        private val _peReader: PEReader)
    : ToolInstanceFactory {

    override fun tryCreate(path: File, baseVersion: Version, platform: Platform)=
            tryCreate(path, baseVersion, path, platform) ?: tryCreate(path, baseVersion, File(path, "Bin"), platform)

    private fun tryCreate(instancePath: File, baseVersion: Version, path: File, platform: Platform): ToolInstance? {
        if (!_fileSystemService.isExists(path) || !_fileSystemService.isDirectory(path)) {
            LOG.debug("Cannot find \"$path\".")
            return null
        }
        else {
            val wsdlFile = File(path, "wsdl.exe")
            if (!_fileSystemService.isExists(wsdlFile) || !_fileSystemService.isFile(wsdlFile)) {
                LOG.debug("Cannot find \"$wsdlFile\".")
                return null
            }
            else {
                var detailedVersion = _peReader.tryGetVersion(wsdlFile)
                if (detailedVersion == Version.Empty) {
                    LOG.warn("Cannot get a product version from \"$wsdlFile\".")
                    detailedVersion = baseVersion
                }

                return ToolInstance(ToolInstanceType.DotNetFrameworkSDK, instancePath, detailedVersion, baseVersion, platform)
            }
        }
    }

    companion object {
        private val LOG = Logger.getLogger(DotnetFrameworkSdkInstanceFactory::class.java)
    }
}