package jetbrains.buildServer.dotnet.commands.resolution

abstract class DotnetCommandStreamResolverBase : DotnetCommandsStreamResolver {
    override final fun resolve(commands: DotnetCommandsStream) = when {
        shouldBeApplied(commands) -> apply(commands)
        else -> commands
    }

    protected abstract fun shouldBeApplied(commands: DotnetCommandsStream): Boolean

    protected abstract fun apply(commands: DotnetCommandsStream): DotnetCommandsStream
}

