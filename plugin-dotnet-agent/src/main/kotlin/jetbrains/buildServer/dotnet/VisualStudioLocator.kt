package jetbrains.buildServer.dotnet

interface VisualStudioLocator {
    val instances: Sequence<VisualStudioInstance>
}