

package jetbrains.buildServer

import java.io.IOException

interface NuGetService {
    @Throws(IOException::class)
    fun getPackagesById(packageId: String): Sequence<NuGetPackage>
}