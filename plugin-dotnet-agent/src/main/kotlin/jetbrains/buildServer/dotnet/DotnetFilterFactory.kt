

package jetbrains.buildServer.dotnet

interface DotnetFilterFactory {
    fun createFilter(context: DotnetCommandContext): DotnetFilter
}