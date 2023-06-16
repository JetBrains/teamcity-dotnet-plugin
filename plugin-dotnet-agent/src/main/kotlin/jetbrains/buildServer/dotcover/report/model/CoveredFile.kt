package jetbrains.buildServer.dotcover.report.model

import jetbrains.coverage.report.CoverageCodeRenderer
import jetbrains.coverage.report.CoverageStatus
import java.util.TreeMap

class CoveredFile(private val _fileId: Int) {

    private val _linesCoverage: MutableMap<Int, CoverageStatus?> = TreeMap()

    fun addLine(line: Int, covered: Boolean) {
        _linesCoverage[line] =
            CoverageStatus.merge(_linesCoverage[line], if (covered) CoverageStatus.FULL else CoverageStatus.NONE)
    }

    fun renderFile(filesMap: DotNetSourceCodeProvider, renderer: CoverageCodeRenderer) {
        val content: Collection<CharSequence> = filesMap.getFileContentLines(_fileId) ?: return

        val caption: String? = filesMap.getCaption(_fileId)
        if (caption != null) {
            renderer.writeSectionHeader(caption)
        }

        var id = 0
        for (line in content) {
            id++
            renderer.writeCodeLine(id, line, _linesCoverage[id])
        }

        renderer.codeWriteFinished()
    }
}
