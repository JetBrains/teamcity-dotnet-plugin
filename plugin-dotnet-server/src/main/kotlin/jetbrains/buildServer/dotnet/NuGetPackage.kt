package jetbrains.buildServer.dotnet

import java.net.URL

data class NuGetPackage(
        val packageId: String,
        val packageVersion: NuGetPackageVersion,
        val downloadUrl: URL,
        val isListed: Boolean)