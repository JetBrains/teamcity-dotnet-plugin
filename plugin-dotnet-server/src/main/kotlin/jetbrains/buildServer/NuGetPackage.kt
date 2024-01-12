

package jetbrains.buildServer

import java.net.URL

data class NuGetPackage(
        val packageId: String,
        val packageVersion: String,
        val downloadUrl: URL)