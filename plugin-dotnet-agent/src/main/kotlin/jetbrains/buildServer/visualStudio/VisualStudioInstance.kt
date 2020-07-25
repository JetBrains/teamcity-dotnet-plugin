package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.agent.Version
import java.io.File

data class VisualStudioInstance(
        val installationPath: File,
        val displayVersion: Version,
        val productLineVersion: Version) {
    override fun toString() = "Visual Studio $productLineVersion($displayVersion) at \"$installationPath\""
}