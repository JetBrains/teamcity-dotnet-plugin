package jetbrains.buildServer.dotcover

import java.io.File

interface DotCoverEntryPointSelector {
    fun select(): Result<File>
}