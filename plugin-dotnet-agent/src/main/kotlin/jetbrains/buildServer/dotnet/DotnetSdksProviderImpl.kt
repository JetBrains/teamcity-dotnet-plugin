package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.FileSystemService
import org.apache.log4j.Logger
import java.io.File

class DotnetSdksProviderImpl(
        private val _fileSystemService: FileSystemService)
    : DotnetSdksProvider {
    override fun getSdks(dotnetExecutable: File): Sequence<DotnetSdk> {
        val sdkPath = File(dotnetExecutable.parent, "sdk")
        LOG.info("Try getting the list of .NET SDK from directory <$sdkPath>.")
        val sdks = _fileSystemService.list(sdkPath)
                .filter { _fileSystemService.isDirectory(it) }
                .map { DotnetSdk(it, Version.parse(it.name)) }
                .filter { it.version != Version.Empty }
        return sdks
    }

    companion object {
        private val LOG = Logger.getLogger(DotnetSdksProviderImpl::class.java.name)
    }
}