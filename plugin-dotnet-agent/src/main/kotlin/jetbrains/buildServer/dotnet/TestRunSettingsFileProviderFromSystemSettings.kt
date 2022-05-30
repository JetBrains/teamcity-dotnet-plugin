package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import java.io.File

class TestRunSettingsFileProviderFromSystemSettings(
        private val _parametersService: ParametersService)
    : TestRunSettingsFileProvider {

    override fun tryGet(command: DotnetCommandType) =
        _parametersService.tryGetParameter(ParameterType.System, SettingSystemParamName)
                ?.let { it.ifBlank { null } }
                ?.let { File(it) }

    companion object {
        internal val SettingSystemParamName = "VSTestSetting"
    }
}