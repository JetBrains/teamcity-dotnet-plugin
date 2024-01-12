

package jetbrains.buildServer.dotnet.commands.test.runSettings

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.test.TestRunSettingsFileProvider
import java.io.File

class TestRunSettingsFileProviderFromParams(
        private val _parametersService: ParametersService)
    : TestRunSettingsFileProvider {

    override fun tryGet(context: DotnetCommandContext) =
        context.command.commandType.takeIf { it == DotnetCommandType.Test || it == DotnetCommandType.VSTest }
                    ?.let {
                        _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_TEST_SETTINGS_FILE)
                                ?.let { it.ifBlank { null } }
                                ?.let { File(it) }
                    }
}