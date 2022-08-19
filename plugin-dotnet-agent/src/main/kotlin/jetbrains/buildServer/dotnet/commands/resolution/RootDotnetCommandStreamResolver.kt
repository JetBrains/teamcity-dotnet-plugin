package jetbrains.buildServer.dotnet.commands.resolution

// The resolver that resolve all other resolvers in a proper order
class RootDotnetCommandStreamResolver(
    private val _dotnetCommandsStreamResolvers: List<DotnetCommandsStreamResolver>,
) : DotnetCommandStreamResolverBase() {
    override val stage = DotnetCommandsStreamResolvingStage.Initial

    override fun shouldBeApplied(commands: DotnetCommandsStream) = true

    override fun apply(commands: DotnetCommandsStream) =
        _dotnetCommandsStreamResolvers
            .sortedBy { it.stage.ordinal }
            .fold(commands) { context, resolver -> resolver.resolve(context) }
}