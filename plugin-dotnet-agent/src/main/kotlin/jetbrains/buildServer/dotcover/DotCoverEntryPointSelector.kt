package jetbrains.buildServer.dotcover

import java.io.File

interface DotCoverEntryPointSelector {
    /**
     * Selects the entry point file for the dotCover tool with agent configuration parameters validation to satisfy dotCover requirements
     *
     * @param skipRequirementsValidation If `true`, skips validation of agent configuration parameters; defaults to `false`
     * @return [Result]<[File]> Result of the selection process, containing the file or an error
     */
    fun select(skipRequirementsValidation: Boolean = false): Result<File>
}