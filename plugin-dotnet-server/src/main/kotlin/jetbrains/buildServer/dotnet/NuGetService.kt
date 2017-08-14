package jetbrains.buildServer.dotnet

import java.io.IOException

interface NuGetService {
    @Throws(IOException::class) fun getPackagesById(packageId: String, allowPrerelease: Boolean = false): Sequence<NuGetPackage>
}