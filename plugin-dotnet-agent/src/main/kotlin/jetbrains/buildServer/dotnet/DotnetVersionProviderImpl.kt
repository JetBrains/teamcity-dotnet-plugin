package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.BuildStepContext
import java.io.File

class DotnetVersionProviderImpl(
        private val _buildStepContext: BuildStepContext,
        private val _commandLineExecutor: CommandLineExecutor,
        private val _versionParser: VersionParser,
        private val _fileSystemService: FileSystemService,
        private val _dotnetToolResolver: DotnetToolResolver)
    : DotnetVersionProvider {

    override fun getVersion(dotnetExecutable: File, path: File): Version {
        LOG.info("Try getting the dotnet CLI version for directory \"$path\".")
        val versionResult = _commandLineExecutor.tryExecute(
                CommandLine(
                        TargetType.Tool,
                        dotnetExecutable,
                        path,
                        versionArgs,
                        emptyList()))

        if (versionResult == null || versionResult.exitCode !=0 || versionResult.errorOutput.filter { it.isNotBlank() }.any()) {
            LOG.error("The error occurred getting the dotnet CLI version.")
        }
        else {
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
        private val LOG = Logger.getInstance(DotnetVersionProviderImpl::class.java.name)
        internal val versionArgs = listOf(CommandLineArgument("--version"))
    }
}