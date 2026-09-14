

package jetbrains.buildServer.agent

import jetbrains.buildServer.util.PEReader.PEUtil
import jetbrains.buildServer.util.PEReader.PEVersion
import java.io.File

class PEReaderImpl : PEReader {
    override fun tryGetVersion(file: File) =
        toVersion(PEUtil.getProductVersion(file))

    override fun tryGetFileVersion(file: File) =
        toVersion(PEUtil.getFileVersion(file))

    private fun toVersion(version: PEVersion?) =
        version?.let { Version(it.p1, it.p2, it.p3, it.p4) } ?: Version.Empty
}
