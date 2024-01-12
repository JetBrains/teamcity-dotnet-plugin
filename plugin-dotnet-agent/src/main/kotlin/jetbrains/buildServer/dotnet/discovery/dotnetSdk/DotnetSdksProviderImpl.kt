

package jetbrains.buildServer.dotnet.discovery.dotnetSdk

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.Logger
import java.io.File

class DotnetSdksProviderImpl(
        private val _fileSystemService: FileSystemService)
    : DotnetSdksProvider {
    override fun getSdks(dotnetExecutable: File): Sequence<DotnetSdk> {
        val sdksPath = File(dotnetExecutable.parent, "sdk")
        if(!_fileSystemService.isExists(sdksPath) || !_fileSystemService.isDirectory(sdksPath)) {
            LOG.warn("The directory <$sdksPath> does not exist.")
        }

        LOG.debug("Try getting the list of .NET SDK from directory <$sdksPath>.")
        return _fileSystemService.list(sdksPath)
                .filter { _fileSystemService.isDirectory(it) }
                .map { DotnetSdk(it, Version.parse(it.name)) }
                .filter { it.version != Version.Empty }
    }

    companion object {
        private val LOG = Logger.getLogger(DotnetSdksProviderImpl::class.java)
    }
}