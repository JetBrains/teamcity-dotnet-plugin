package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import java.io.File

class TestRunSettingsFileProviderFromTestArgs(
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService)
    : TestRunSettingsFileProvider {

    override fun tryGet(command: DotnetCommandType) =
        command.takeIf { it == DotnetCommandType.Test }
                ?.let {
                    _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_ARGUMENTS)
                            ?.trim()
                            ?.let { _argumentsService.split(it) }
                            ?.filter { it.isNotBlank() }
                            ?.map { it.trim() }
                            ?.toList()
                            ?.let { args ->
                                args
                                        .indexOfLast { "--settings".equals(it, true) || "-s".equals(it, true) }
                                        .takeIf { it >= 0 && it < args.size - 1 }
                                        ?.let { args[it + 1] }
                                        ?.let { File(it) }
                            }
                }
}