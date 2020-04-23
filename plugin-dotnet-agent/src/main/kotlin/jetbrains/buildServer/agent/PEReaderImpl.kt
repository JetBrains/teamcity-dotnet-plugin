package jetbrains.buildServer.agent

import jetbrains.buildServer.util.PEReader.PEUtil
import jetbrains.buildServer.util.PEReader.PEVersion
import java.io.File

class PEReaderImpl : PEReader {
    override fun tryGetProductVersion(file: File) =
        PEUtil.getProductVersion(file)
}