package jetbrains.buildServer.agent

import jetbrains.buildServer.util.PEReader.PEUtil
import jetbrains.buildServer.util.PEReader.PEVersion
import java.io.File

class PEReaderImpl : PEReader {
    override fun tryGetVersion(file: File) =
        PEUtil.getProductVersion(file)?.let {
            Version(it.p1, it.p2, it.p3, it.p4)
        } ?: Version.Empty
}