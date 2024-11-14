package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.runner.BuildStepContext

class RestorePackagesPathArgumentsProvider(
    private val _restorePackagesPathManager: RestorePackagesPathManager,
    private val _buildStepContext: BuildStepContext
) : ArgumentsProvider {

    override fun getArguments(context: DotnetCommandContext): Sequence<CommandLineArgument> = sequence {
        if (_restorePackagesPathManager.shouldOverrideRestorePackagesPath()) {
            val agentConfiguration = _buildStepContext.runnerContext.build.agentConfiguration
            yield(
                CommandLineArgument(
                    "-p:RestorePackagesPath=${_restorePackagesPathManager.getRestorePackagesPathLocation(agentConfiguration).absolutePath}",
                    CommandLineArgumentType.Infrastructural
                )
            )
        }
    }
}