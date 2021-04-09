package jetbrains.buildServer.visualStudio

interface VisualStudioPackagesLocator {
    fun tryGetPackagesPath(): String?
}