package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.util.OSType
import org.apache.log4j.Logger
import java.io.File

class EnvironmentVariablesImpl(
        private val _environment: Environment,
        private val _pathsService: PathsService,
        private val _fileSystemService: FileSystemService,
        private val _virtualContext: VirtualContext)
    : EnvironmentVariables {
    override fun getVariables(sdkVersion: Version): Sequence<CommandLineEnvironmentVariable> = sequence {
        yieldAll(defaultVariables)
        yield(CommandLineEnvironmentVariable("NUGET_PACKAGES", _virtualContext.resolvePath(File(File(_pathsService.getPath(PathType.System), "dotnet"), ".nuget").canonicalPath)))

        val home = if (_environment.os == OSType.WINDOWS) "USERPROFILE" else "HOME"
        if (_environment.tryGetVariable(home).isNullOrEmpty()) {
            yield(CommandLineEnvironmentVariable(home, System.getProperty("user.home")))
        }

        if (_virtualContext.targetOSType != OSType.WINDOWS) {
            if (_virtualContext.isVirtual && _environment.os == OSType.WINDOWS) {
                LOG.debug("Override environment variable like 'TMP' by empty values")
                yieldAll(getTempDirVariables())
            } else {
                val tempPath = _pathsService.getPath(PathType.BuildTemp).path
                if (tempPath.length <= 60) {
                    LOG.debug("Do not override environment variable like 'TMP'")
                }
                else {
                    // try to find default /tmp
                    if (_fileSystemService.isExists(defaultTemp) && _fileSystemService.isDirectory(defaultTemp)) {
                        LOG.debug("Override environment variable like 'TMP' by '${defaultTemp.path}'")
                        yieldAll(getTempDirVariables(defaultTemp.path))
                    } else {
                        // create short TemamCity temp
                        if (!_fileSystemService.isExists(customTeamCityTemp)) {
                            _fileSystemService.createDirectory(customTeamCityTemp)
                        }

                        LOG.debug("Override environment variable like 'TMP' by '${customTeamCityTemp.canonicalPath}'")
                        yieldAll(getTempDirVariables(customTeamCityTemp.canonicalPath))
                    }
                }
            }
        }
    }

    companion object {
        private val LOG = Logger.getLogger(EnvironmentVariablesImpl::class.java)

        internal val defaultVariables = sequenceOf(
                CommandLineEnvironmentVariable("UseSharedCompilation", "false"),
                CommandLineEnvironmentVariable("COMPlus_EnableDiagnostics", "0"),
                CommandLineEnvironmentVariable("DOTNET_CLI_TELEMETRY_OPTOUT", "true"),
                CommandLineEnvironmentVariable("DOTNET_SKIP_FIRST_TIME_EXPERIENCE", "true"),
                CommandLineEnvironmentVariable("NUGET_XMLDOC_MODE", "skip"))

        internal fun getTempDirVariables(tempPath: String = "") = sequenceOf(
                CommandLineEnvironmentVariable("TEMP", tempPath),
                CommandLineEnvironmentVariable("TMP", tempPath),
                CommandLineEnvironmentVariable("TMPDIR", tempPath))

        internal val defaultTemp = File("/tmp")
        internal val customTeamCityTemp = File("~/teamcity_temp")
    }
}