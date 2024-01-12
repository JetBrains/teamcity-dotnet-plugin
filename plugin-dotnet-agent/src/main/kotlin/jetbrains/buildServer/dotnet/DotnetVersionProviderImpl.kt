

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.toolResolvers.DotnetToolResolver

class DotnetVersionProviderImpl(
    private val _buildStepContext: BuildStepContext,
    private val _commandLineExecutor: CommandLineExecutor,
    private val _versionParser: ToolVersionOutputParser,
    private val _fileSystemService: FileSystemService,
    private val _dotnetToolResolver: DotnetToolResolver
)
    : DotnetVersionProvider {

    override fun getVersion(dotnetExecutable: Path, workingDirectory: Path): Version {
        LOG.debug("Try getting the dotnet CLI version for directory \"$workingDirectory\".")
        val versionResult = _commandLineExecutor.tryExecute(
                CommandLine(
                        null,
                        TargetType.Tool,
                        dotnetExecutable,
                        workingDirectory,
                        versionArgs,
                        emptyList()))

        if (versionResult == null || versionResult.isError) {
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
        private val LOG = Logger.getLogger(DotnetVersionProviderImpl::class.java)
        internal val versionArgs = listOf(CommandLineArgument("--version"))
    }
}