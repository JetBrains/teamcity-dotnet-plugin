package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import java.io.File

class DotnetCliToolInfoImpl(
        private val _toolProvider: ToolProvider,
        private val _commandLineExecutor: CommandLineExecutor,
        private val _versionParser: VersionParser)
    : DotnetCliToolInfo {
    override fun getVersion(path: File): Version =
            _commandLineExecutor.tryExecute(
                    CommandLine(
                            TargetType.Tool,
                            File(_toolProvider.getPath(DotnetConstants.EXECUTABLE)),
                            path,
                            versionArgs,
                            emptyList()))
                    ?.let {
                        _versionParser.tryParse(it.standardOutput)?.let {
                            Version.parse(it)
                        }
                    } ?: jetbrains.buildServer.dotnet.Version.Empty

    companion object {
        internal val versionArgs = listOf(CommandLineArgument("--version"))
    }
}