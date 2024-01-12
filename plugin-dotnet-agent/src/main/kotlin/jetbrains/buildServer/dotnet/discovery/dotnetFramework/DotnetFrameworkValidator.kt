

package jetbrains.buildServer.dotnet.discovery.dotnetFramework

interface DotnetFrameworkValidator {
    fun isValid(framework: DotnetFramework): Boolean
}