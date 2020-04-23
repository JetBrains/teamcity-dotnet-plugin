package jetbrains.buildServer.dotnet

interface VisualStudioPackagesLocator {
    fun tryGetPackagesPath(): String?
}