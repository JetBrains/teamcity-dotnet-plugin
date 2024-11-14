package jetbrains.buildServer.dotnet.commands.transformation

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineOutputAccumulationObserver
import jetbrains.buildServer.agent.CommandResultEvent
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.depcache.DotnetDepCacheStepContext
import jetbrains.buildServer.depcache.DotnetDepCacheManager
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.ListPackageCommand
import jetbrains.buildServer.dotnet.commands.NugetLocalsCommand
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.rx.Observer
import jetbrains.buildServer.rx.disposableOf

class DotnetDepCacheRestorationCommandTransformer(
    private val _nugetLocalsCommand: NugetLocalsCommand,
    private val _listPackageCommand: ListPackageCommand,
    private val _dotnetDepCacheManager: DotnetDepCacheManager,
    private val _restorePackagesPathArgumentsProvider: ArgumentsProvider,
    private val _restorePackagesPathManager: RestorePackagesPathManager,
    private val _buildStepContext: BuildStepContext,
    private val _virtualContext: VirtualContext
) : DotnetCommandsTransformer {

    override val stage = DotnetCommandsTransformationStage.DepCacheRestoration

    override fun apply(context: DotnetCommandContext, commands: DotnetCommandsStream): DotnetCommandsStream {
        return sequence {
            var depCacheContext = DotnetDepCacheStepContext.newContext()
            for (initialCommand in commands) {
                val shouldTransform = _dotnetDepCacheManager.cacheEnabled &&
                        versionCompatible(context.toolVersion) &&
                        commandDepCacheCompatible(initialCommand) &&
                        targetArgumentsDepCacheCompatible(initialCommand)

                if (shouldTransform) {
                    registerAndRestoreCache(depCacheContext)

                    // executing the initial command
                    yield(OverriddenRestorePackagesPathDotnetCommand(initialCommand, _restorePackagesPathArgumentsProvider, overrideRestorePackagesPath))

                    // executing a 'dotnet list package' auxiliary command after the initial one to update invalidation data with actual packages list
                    // it must be executed after the initial command to ensure that all the packages have been restored by the moment of execution
                    val projectPackagesObserver = CommandLineOutputAccumulationObserver()
                    val updateInvalidationDataEnvBuilder = getUpdateInvalidationDataEnvBuilder(depCacheContext, projectPackagesObserver)
                    yield(ObservingDotnetCommand(_listPackageCommand, projectPackagesObserver, listOf(updateInvalidationDataEnvBuilder), initialCommand.targetArguments))
                } else {
                    yield(initialCommand)
                }
            }
        }
    }

    private suspend fun SequenceScope<DotnetCommand>.registerAndRestoreCache(depCacheContext: DotnetDepCacheStepContext) {
        if (overrideRestorePackagesPath) {
            // since the RestorePackagesPath was overridden by TC, the cache root location is known, so the dependency cache can be registered and restored immediately
            val agentConfiguration = _buildStepContext.runnerContext.build.agentConfiguration
            val restorePackagesPath = _restorePackagesPathManager.getRestorePackagesPathLocation(agentConfiguration)
            _dotnetDepCacheManager.cache?.logMessage("running the build inside a Docker container with enabled package caching: setting the RestorePackagesPath MSBuild property to $restorePackagesPath. " +
                    "The property will be reset to its initial value once the build finishes")
            _dotnetDepCacheManager.registerAndRestoreCache(depCacheContext, restorePackagesPath)
        } else {
            // execute a 'nuget locals' auxiliary command before the initial one to detect the cache root location, then register and restore the dependency cache
            val nugetPackagesGlobalDirObserver = CommandLineOutputAccumulationObserver()
            val registerCacheEnvBuilder = getRegisterCacheEnvBuilder(depCacheContext, nugetPackagesGlobalDirObserver)
            yield(ObservingDotnetCommand(_nugetLocalsCommand, nugetPackagesGlobalDirObserver, listOf(registerCacheEnvBuilder)))
        }
    }

    private val overrideRestorePackagesPath
        get() = _restorePackagesPathManager.shouldOverrideRestorePackagesPath() &&
                _dotnetDepCacheManager.cacheEnabled &&
                _virtualContext.isVirtual

    private fun getRegisterCacheEnvBuilder(
        depCacheContext: DotnetDepCacheStepContext,
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
        depCacheContext: DotnetDepCacheStepContext,
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

    private fun versionCompatible(toolVersion: Version): Boolean {
        val compatible = toolVersion != Version.Empty && toolVersion >= MinDotNetSdkVersionForDepCache
        if (!compatible) {
            _dotnetDepCacheManager.cache?.logWarning(
                """the dependency cache is enabled but couldn't be used.
                Please update the .NET version.
                The minimum required .NET version for the dependency cache: ${MinDotNetSdkVersionForDepCache.toString()}.
                The version used for the build: ${toolVersion.toString()}.
                 """.trimIndent()
            )
        }
        return compatible
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

    private class OverriddenRestorePackagesPathDotnetCommand(
        private val _originalCommand: DotnetCommand,
        private val _restorePackagesPathArgumentsProvider: ArgumentsProvider,
        private val _overrideRestorePackagesPath: Boolean
    ) : DotnetCommand by _originalCommand {
        override fun getArguments(context: DotnetCommandContext): Sequence<CommandLineArgument> {
            if (_overrideRestorePackagesPath) {
                return _originalCommand.getArguments(context) + _restorePackagesPathArgumentsProvider.getArguments(context)
            }
            return _originalCommand.getArguments(context)
        }
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