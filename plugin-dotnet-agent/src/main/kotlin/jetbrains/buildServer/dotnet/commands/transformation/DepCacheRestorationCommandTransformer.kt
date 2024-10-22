package jetbrains.buildServer.dotnet.commands.transformation

import jetbrains.buildServer.agent.CommandLineOutputAccumulationObserver
import jetbrains.buildServer.agent.CommandResultEvent
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.depcache.DependencyCacheDotnetStepContext
import jetbrains.buildServer.depcache.DotnetDependencyCacheConstants
import jetbrains.buildServer.depcache.DotnetDependencyCacheManager
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.ListPackageCommand
import jetbrains.buildServer.dotnet.commands.NugetLocalsCommand
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.rx.Observer
import jetbrains.buildServer.rx.disposableOf

class DepCacheRestorationCommandTransformer(
    private val _parametersService: ParametersService,
    private val _nugetLocalsCommand: NugetLocalsCommand,
    private val _listPackageCommand: ListPackageCommand,
    private val _dotnetDepCacheManager: DotnetDependencyCacheManager,
    private val _buildStepContext: BuildStepContext
) : DotnetCommandsTransformer {

    override val stage = DotnetCommandsTransformationStage.DepCacheRestoration

    override fun apply(context: DotnetCommandContext, commands: DotnetCommandsStream): DotnetCommandsStream {
        if (!shouldBeApplied(context.toolVersion)) {
            return commands
        }

        return sequence {
            var depCacheContext = DependencyCacheDotnetStepContext.newContext()
            for (initialCommand in commands) {
                if (commandDepCacheCompatible(initialCommand)) {
                    // executing a 'nuget locals' auxiliary command before the initial one to register and restore the dependency cache
                    val nugetPackagesGlobalDirObserver = CommandLineOutputAccumulationObserver()
                    val registerCacheEnvBuilder = getRegisterCacheEnvBuilder(depCacheContext, nugetPackagesGlobalDirObserver)
                    yield(ObservingDotnetCommand(_nugetLocalsCommand, nugetPackagesGlobalDirObserver, listOf(registerCacheEnvBuilder)))
                }

                // executing the initial command
                yield(initialCommand)

                if (commandDepCacheCompatible(initialCommand) && targetArgumentsDepCacheCompatible(initialCommand)) {
                    // executing a 'dotnet list package' auxiliary command after the initial one to update invalidation data with actual packages list
                    // it must be executed after the initial command to ensure that all the packages have been restored by the moment of execution
                    val projectPackagesObserver = CommandLineOutputAccumulationObserver()
                    val updateInvalidationDataEnvBuilder = getUpdateInvalidationDataEnvBuilder(depCacheContext, projectPackagesObserver)
                    yield(ObservingDotnetCommand(_listPackageCommand, projectPackagesObserver, listOf(updateInvalidationDataEnvBuilder), initialCommand.targetArguments))
                }
            }
        }
    }

    private fun getRegisterCacheEnvBuilder(
        depCacheContext: DependencyCacheDotnetStepContext,
        nugetPackagesGlobalDirObserver: CommandLineOutputAccumulationObserver
    ) = object : EnvironmentBuilder {
        override fun build(context: DotnetCommandContext): EnvironmentBuildResult {
            return EnvironmentBuildResult(
                // will be executed after the command
                disposable = disposableOf {
                    _dotnetDepCacheManager.registerAndRestoreCache(depCacheContext, nugetPackagesGlobalDirObserver)
                }
            )
        }
    }

    private fun getUpdateInvalidationDataEnvBuilder(
        depCacheContext: DependencyCacheDotnetStepContext,
        projectPackagesObserver: CommandLineOutputAccumulationObserver
    ) = object : EnvironmentBuilder {
        override fun build(context: DotnetCommandContext): EnvironmentBuildResult {
            return EnvironmentBuildResult(
                // will be executed after the command
                disposable = disposableOf {
                    _dotnetDepCacheManager.updateInvalidationData(depCacheContext, projectPackagesObserver)
                }
            )
        }
    }

    private fun shouldBeApplied(toolVersion: Version): Boolean {
        if (_dotnetDepCacheManager.cache == null) { // not null when cache is enabled and configured for dotnet runner
            return false
        }
        if (!versionCompatible(toolVersion)) {
            _dotnetDepCacheManager.cache?.logWarning(
                """the dependency cache is enabled but couldn't be used.
                Please update the .NET version.
                The minimum required .NET version for the dependency cache: ${MinDotNetSdkVersionForDepCache.toString()}.
                The version used for the build: ${toolVersion.toString()}.
                 """.trimIndent()
            )
            return false
        }
        return true
    }

    private fun versionCompatible(toolVersion: Version): Boolean {
        return toolVersion != Version.Empty && toolVersion >= MinDotNetSdkVersionForDepCache
    }

    private fun String.endsWithIgnoreCase(suffix: String): Boolean {
        return this.lowercase().endsWith(suffix.lowercase())
    }

    private fun targetArgumentsDepCacheCompatible(command: DotnetCommand): Boolean {
        return command.targetArguments.flatMap { it.arguments }.none { argument ->
            UnsupportedTargets.any { unsupported ->
                argument.value.endsWithIgnoreCase(unsupported)
            }
        }
    }

    private fun commandDepCacheCompatible(command: DotnetCommand) = !UnsupportedCommands.contains(command.commandType)

    private class ObservingDotnetCommand(
        private val _originalCommand: DotnetCommand,
        private val _resultObserver: Observer<CommandResultEvent>,
        override val environmentBuilders: List<EnvironmentBuilder>,
        private val _targetArguments: Sequence<TargetArguments> = emptySequence()
    ) : DotnetCommand by _originalCommand {
        override val resultsObserver = _resultObserver
        override val targetArguments: Sequence<TargetArguments> get() = _targetArguments
    }

    companion object {
        // It is currently limited by the 'list package' command, which we use to obtain the package list.
        // The --format option is only available starting from version 7.0.200
        // see https://learn.microsoft.com/en-us/dotnet/core/tools/dotnet-list-package
        val MinDotNetSdkVersionForDepCache = Version(7, 0, 200)

        private val UnsupportedCommands = setOf(
            DotnetCommandType.VisualStudio,
            DotnetCommandType.Custom,
            DotnetCommandType.VSTest,
            DotnetCommandType.NuGetPush,
            DotnetCommandType.NuGetDelete
        )

        private val UnsupportedTargets = setOf(".dll", ".exe")
    }
}