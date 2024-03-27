package jetbrains.buildServer.dotcover

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.util.OSType
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.DotnetConstants
import java.io.File

class DotCoverEnvironmentVariables(
    private val _environment: Environment,
    private val _virtualContext: VirtualContext,
    private val _fileSystemService: FileSystemService,
    private val _pathsService: PathsService,
    private val _parametersService: ParametersService
) : EnvironmentVariables {
    override fun getVariables(): Sequence<CommandLineEnvironmentVariable> = sequence {
        if (_virtualContext.targetOSType == OSType.UNIX) {
            yieldAll(linuxDefaultVariables)
        }

        // dotCover doesn't work with long temp dir paths on Unix-like systems
        // because tries to create named pipes with short struct. 60 symbols only
        if (_virtualContext.targetOSType != OSType.WINDOWS) {
            if (_virtualContext.isVirtual && _environment.os == OSType.WINDOWS) {
                // Overriding temp environment variables by empty values leads to a PDB server error
                // Therefore, we disable this behaviour and hide behind the feature toggle
                // see https://youtrack.jetbrains.com/issue/TW-86663
                if (overrideTempDirWithEmptyValue) {
                    LOG.debug("Override temp environment variables by empty values")
                    yieldAll(getTempDirVariables())
                } else {
                    return@sequence
                }
            } else {
                val tempPath = _pathsService.getPath(PathType.BuildTemp).path
                if (tempPath.length <= 60) {
                    LOG.debug("Do not override temp environment variables")
                }
                else {
                    // try to find default /tmp
                    if (_fileSystemService.isExists(defaultTemp) && _fileSystemService.isDirectory(defaultTemp)) {
                        LOG.debug("Override temp environment variables by '${defaultTemp.path}'")
                        yieldAll(getTempDirVariables(defaultTemp.path))
                    } else {
                        // create short TeamCity temp
                        if (!_fileSystemService.isExists(customTeamCityTemp)) {
                            _fileSystemService.createDirectory(customTeamCityTemp)
                        }

                        LOG.debug("Override temp environment variables by '${customTeamCityTemp}'")
                        yieldAll(getTempDirVariables(customTeamCityTemp.path))
                    }
                }
            }
        }
    }

    val overrideTempDirWithEmptyValue get() =
        _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_OVERRIDING_TEMP_DIR_WITH_EMPTY_VALUE_ENABLED)
            ?.lowercase()
            ?.trim()
            ?.toBooleanStrictOrNull()
            ?: false

    companion object {
        private val LOG = Logger.getLogger(DotCoverEnvironmentVariables::class.java)

        internal val linuxDefaultVariables = sequenceOf(CommandLineEnvironmentVariable("LC_ALL", "C"))

        internal fun getTempDirVariables(tempPath: String = "") = sequenceOf(
            CommandLineEnvironmentVariable("TEMP", tempPath),
            CommandLineEnvironmentVariable("TMP", tempPath),
            CommandLineEnvironmentVariable("TMPDIR", tempPath)
        )

        internal val defaultTemp = File("/tmp")
        internal val customTeamCityTemp = File("~/teamcity_temp")
    }
}