package jetbrains.buildServer.dotnet

interface NuGetPackageVersionParser {
    fun tryParse(version: String): NuGetPackageVersion?
}