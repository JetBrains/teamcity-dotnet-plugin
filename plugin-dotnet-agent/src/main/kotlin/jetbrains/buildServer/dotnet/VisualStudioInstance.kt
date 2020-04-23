package jetbrains.buildServer.dotnet

data class VisualStudioInstance(
        val installationPath: String,
        val displayVersion: String,
        val productLineVersion: String)