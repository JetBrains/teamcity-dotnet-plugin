package jetbrains.buildServer.dotcover.command

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.util.OSType

abstract class DotCoverCommandLineBuilderBase(
    private val _pathsService: PathsService,
    private val _virtualContext: VirtualContext,
    private val _parametersService: ParametersService,
    private val _fileSystemService: FileSystemService
) : DotCoverCommandLineBuilder {

    protected val workingDirectory get() =
        Path(_pathsService.getPath(PathType.WorkingDirectory).canonicalPath)

    protected val argumentPrefix get() = when(_virtualContext.targetOSType) {
        OSType.WINDOWS -> "/"
        else -> "--"
    }

    protected val logFileName get() =
        _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH)?.let {
            val logFileName = _virtualContext.resolvePath(_fileSystemService.generateTempFile(java.io.File(it), "dotCover", ".log").canonicalPath)
            return@let logFileName
        }
}