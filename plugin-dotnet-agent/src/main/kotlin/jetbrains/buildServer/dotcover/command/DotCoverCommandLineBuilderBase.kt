package jetbrains.buildServer.dotcover.command

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotcover.tool.DotCoverAgentTool
import jetbrains.buildServer.dotcover.tool.DotCoverToolType
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.util.OSType

abstract class DotCoverCommandLineBuilderBase(
    private val _pathsService: PathsService,
    private val _virtualContext: VirtualContext,
    private val _parametersService: ParametersService,
    private val _fileSystemService: FileSystemService,
    private val _dotCoverAgentTool : DotCoverAgentTool,
) : DotCoverCommandLineBuilder {

    protected val workingDirectory get() =
        Path(_pathsService.getPath(PathType.WorkingDirectory).canonicalPath)

    protected val argumentPrefix get(): String {
        if (_dotCoverAgentTool.type == DotCoverToolType.CrossPlatformV3) {
            return "--" // For DotCover 2025.2+, it's always "--" no matter the OS
        }
        return when (_virtualContext.targetOSType) {
            OSType.WINDOWS -> "/"
            else -> "--"
        }
    }

    /**
     * Provides a prefix for a filename that contains command line parameters.
     * The prefix is only needed for the new versions of DotCover (2025.2 and newer).
     *
     * Example of using the prefix '@':
     * ```bash
     * dotCover cover @args.txt
     * ```
     *
     * @see <a href="https://www.jetbrains.com/help/dotcover/2025.3/dotCover__Console_Runner_Commands.html">DotCover command line reference</a>
     */
    protected val commandLineParametersFilePrefix get() = when(_dotCoverAgentTool.type) {
        DotCoverToolType.CrossPlatformV3 -> "@"
        else -> ""
    }

    protected val logFileName get() =
        _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH)?.let {
            val logFileName = _virtualContext.resolvePath(_fileSystemService.generateTempFile(java.io.File(it), "dotCover", ".log").canonicalPath)
            return@let logFileName
        }
}