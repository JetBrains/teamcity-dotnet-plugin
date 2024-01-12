

package jetbrains.buildServer.dotnet.commands.test.runSettings

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.commands.test.TestRunSettingsFileProvider
import java.io.File

class TestRunSettingsFileProviderFromSystemSettings(
        private val _parametersService: ParametersService)
    : TestRunSettingsFileProvider {

    override fun tryGet(context: DotnetCommandContext) =
        _parametersService.tryGetParameter(ParameterType.System, SettingSystemParamName)
                ?.let { it.ifBlank { null } }
                ?.let { File(it) }

    companion object {
        internal val SettingSystemParamName = "VSTestSetting"
    }
}