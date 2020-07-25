package jetbrains.buildServer.visualStudio

interface VisualStudioProvider {
    fun getInstances(): Sequence<VisualStudioInstance>
}