package jetbrains.buildServer

import jetbrains.buildServer.dotnet.SemanticVersion
import java.net.URL

data class NuGetPackage(
        val packageId: String,
        val packageVersion: SemanticVersion,
        val downloadUrl: URL,
        val isListed: Boolean)