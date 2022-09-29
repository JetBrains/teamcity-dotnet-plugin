package jetbrains.buildServer.dotnet.commands.test.runSettings

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.commands.test.TestRunSettingsFileProvider
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