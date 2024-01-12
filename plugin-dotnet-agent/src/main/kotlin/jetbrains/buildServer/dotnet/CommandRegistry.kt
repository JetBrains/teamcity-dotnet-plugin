

package jetbrains.buildServer.dotnet

interface CommandRegistry {
    fun register(context: DotnetCommandContext)
}