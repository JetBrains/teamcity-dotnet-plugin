package jetbrains.buildServer.dotcover

import jetbrains.buildServer.Serializer

/**
 * Interface for serializing a [DotCoverProject] into a response file for storing
 * DotCover command line parameters in accordance with the
 * [Command Line Reference](https://www.jetbrains.com/help/dotcover/2025.3/dotCover__Console_Runner_Commands.html).
 *
 * Note that the response file format is only supported for versions *2025.2 and newer*.
 * To store command line parameters for versions 2025.1 and earlier, use [DotCoverRunConfigFileSerializer].
 */
interface DotCoverResponseFileSerializer : Serializer<DotCoverProject>