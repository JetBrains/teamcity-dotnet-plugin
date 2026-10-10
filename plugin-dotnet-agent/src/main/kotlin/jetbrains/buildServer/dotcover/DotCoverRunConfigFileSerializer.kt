package jetbrains.buildServer.dotcover

import jetbrains.buildServer.Serializer

/**
 * Interface for serializing a [DotCoverProject] into an XML configuration for storing
 * DotCover command line parameters in accordance with the
 * [Command Line Reference](https://www.jetbrains.com/help/dotcover/2025.1/dotCover__Console_Runner_Commands.html).
 *
 * Note that the XML configuration produced with this serializer is only supported for versions *2025.1 and earlier*.
 * To store command line parameters for versions 2025.2 and newer, use [DotCoverResponseFileSerializer].
 */
interface DotCoverRunConfigFileSerializer : Serializer<DotCoverProject>