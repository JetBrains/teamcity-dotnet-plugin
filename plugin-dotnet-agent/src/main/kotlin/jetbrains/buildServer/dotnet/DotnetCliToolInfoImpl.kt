package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.*
import java.io.File

class DotnetCliToolInfoImpl(
        private val _commandLineExecutor: CommandLineExecutor,
        private val _versionParser: VersionParser,
        private val _fileSystemService: FileSystemService,
        private val _sdkPathProvider: SdkPathProvider)
    : DotnetCliToolInfo {

    override fun getInfo(dotnetExecutable: File, path: File): DotnetInfo {
        val version = getVersion(dotnetExecutable, path)

        val sdks = _fileSystemService.list(_sdkPathProvider.path)
                .filter { _fileSystemService.isDirectory(it) }
                .map { DotnetSdk(it, Version.parse(it.name)) }
                .filter { it.version != Version.Empty }

        return DotnetInfo(version, sdks.toList())
    }


    private fun getVersion(dotnetExecutable: File, path: File): Version {
        LOG.info("Try getting the dotnet CLI version for directory \"$path\".")
        val versionResult = _commandLineExecutor.tryExecute(
                CommandLine(
                        TargetType.Tool,
                        dotnetExecutable,
                        path,
                        versionArgs,
                        emptyList()))

        if (versionResult == null) {
            LOG.error("The error occurred getting the dotnet CLI version.")
        } else {
            val version = _versionParser.tryParse(versionResult.standardOutput)?.let {
                Version.parse(it)
            }

            if (version == null) {
                LOG.error("The error occurred parsing the dotnet CLI version.")
            } else {
                return version
            }
        }

        return Version.Empty
    }

    companion object {
        private val LOG = Logger.getInstance(DotnetCliToolInfoImpl::class.java.name)
        internal val versionArgs = listOf(CommandLineArgument("--version"))

    }
}