

package jetbrains.buildServer.dotnet

import java.io.File

data class DotnetFilter(
    val filter: String,
    val settingsFile: File?,
    val isSplittingByFilter: Boolean,
)