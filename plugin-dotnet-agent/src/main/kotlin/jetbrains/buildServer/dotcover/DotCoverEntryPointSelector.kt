package jetbrains.buildServer.dotcover

import java.io.File

interface DotCoverEntryPointSelector {
    /**
     * Selects the entry point file for the dotCover tool with agent configuration parameters validation to satisfy dotCover requirements
     *
     * @return [Result]<[File]> Result of the selection process, containing the file or an error
     */
    fun select(): Result<File>
}