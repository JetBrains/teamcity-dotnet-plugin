package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import java.io.File

class TestRunSettingsFileProviderFromParams(
        private val _parametersService: ParametersService)
    : TestRunSettingsFileProvider {

    override fun tryGet(command: DotnetCommandType) =
            command.takeIf { it == DotnetCommandType.Test || it == DotnetCommandType.VSTest }
                    ?.let {
                        _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_TEST_SETTINGS_FILE)
                                ?.let { it.ifBlank { null } }
                                ?.let{ File(it) }
                    }
}