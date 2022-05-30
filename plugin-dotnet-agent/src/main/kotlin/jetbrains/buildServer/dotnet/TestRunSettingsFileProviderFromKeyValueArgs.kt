package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import java.io.File

class TestRunSettingsFileProviderFromKeyValueArgs(
        _args: List<String>,
        private val _commands: List<DotnetCommandType>,
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService)
    : TestRunSettingsFileProvider {

    private var _argRegex: Regex

    init {
        _argRegex = Regex("^(${_args.joinToString("|")})\\s*(.+)\\s*\$", RegexOption.IGNORE_CASE)
    }

    override fun tryGet(command: DotnetCommandType) =
            command.takeIf { _commands.contains(command) }
                    ?.let {
                        _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_ARGUMENTS)
                                ?.trim()
                                ?.let {_argumentsService.split(it) }
                                ?.mapNotNull { _argRegex.matchEntire(it) }
                                ?.filter { it.groupValues.size == 3 }
                                ?.map { File(it.groupValues[2]) }
                                ?.lastOrNull()
                    }
}