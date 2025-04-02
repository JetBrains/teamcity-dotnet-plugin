package jetbrains.buildServer.nunit

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.nunit.NUnitRunnerConstants.MIN_NUPKG_VERSION
import jetbrains.buildServer.nunit.NUnitRunnerConstants.OVERWRITE_TEAMCITY_EVENT_LISTENER
import jetbrains.buildServer.nunit.toolState.NUnitToolState
import jetbrains.buildServer.util.VersionComparatorUtil
import java.io.File
import kotlin.io.path.notExists

class NUnitTeamCityEventListenerExtensionPreparer(
    private val _nunitSettings: NUnitSettings,
    private val _loggerService: LoggerService,
    private val _pathsService: PathsService,
    private val _fileSystemService: FileSystemService,
    private val _parametersService: ParametersService
) {

    fun ensureExtensionPresent(toolState: NUnitToolState) {
        val version = toolState.nUnitVersion
        if (VersionComparatorUtil.compare(version, MIN_NUPKG_VERSION) < 0) {
            _loggerService.writeDebug("For versions earlier than $MIN_NUPKG_VERSION, TeamCityEventListener is expected to be included in the NUnit Console distribution. " +
                    "Skipping copy")
            return
        }

        _loggerService.writeDebug("TeamCityEventListener is no longer distributed as of version: $version. Copying it to the tool directory")

        copyExtensionToNunitDir()
        toolState.extensions.add("NUnit.Engine.Listeners.TeamCityEventListener")
    }

    private fun copyExtensionToNunitDir() {
        val extensionTarget = File(_nunitSettings.nUnitPath, EXTENSION_DIR_NAME)
        val alwaysOverwriteListener = _parametersService.tryGetParameter(ParameterType.Configuration, OVERWRITE_TEAMCITY_EVENT_LISTENER)
            ?.toBooleanStrictOrNull()
            ?: false
        if (extensionTarget.exists()) {
            when (alwaysOverwriteListener) {
                true -> _fileSystemService.remove(extensionTarget)
                false -> {
                    _loggerService.writeDebug("No need to copy $EXTENSION_DIR_NAME: it's already present")
                    return
                }
            }
        }

        val extensionSourcePath = _pathsService.resolvePath(PathType.Plugin, "tools/$EXTENSION_DIR_NAME")
        if (extensionSourcePath.notExists()) {
            _loggerService.writeWarning("TeamCityEventListener is missing from the following directory and could not be copied: $extensionSourcePath")
            return
        }

        _fileSystemService.copy(extensionSourcePath.toFile(), extensionTarget)
    }

    companion object {
        private const val EXTENSION_DIR_NAME = "NUnit.Extension.TeamCityEventListener"
    }
}