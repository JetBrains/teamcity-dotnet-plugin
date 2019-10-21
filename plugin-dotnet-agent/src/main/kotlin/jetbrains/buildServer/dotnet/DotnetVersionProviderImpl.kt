package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.BuildStepContext
import org.apache.log4j.Logger

class DotnetVersionProviderImpl(
        private val _buildStepContext: BuildStepContext,
        private val _commandLineExecutor: CommandLineExecutor,
        private val _versionParser: VersionParser,
        private val _fileSystemService: FileSystemService,
        private val _dotnetToolResolver: DotnetToolResolver)
    : DotnetVersionProvider {

    override fun getVersion(dotnetExecutable: Path, workingDirectory: Path): Version {
        LOG.info("Try getting the dotnet CLI version for directory \"$workingDirectory\".")
        val versionResult = _commandLineExecutor.tryExecute(
                CommandLine(
                        TargetType.Tool,
                        dotnetExecutable,
                        workingDirectory,
                        versionArgs,
                        emptyList()))

        if (versionResult == null || versionResult.exitCode !=0 || versionResult.errorOutput.filter { it.isNotBlank() }.any()) {
            LOG.warn("The error occurred getting the dotnet CLI version.")
            return Version.Empty
        }
        else {
            val version = _versionParser.parse(versionResult.standardOutput)
            if (version == Version.Empty) {
                LOG.warn("The error occurred parsing the dotnet CLI version.")
            }

            return version
        }
    }


    companion object {
        private val LOG = Logger.getLogger(DotnetVersionProviderImpl::class.java.name)
        internal val versionArgs = listOf(CommandLineArgument("--version"))
    }
}