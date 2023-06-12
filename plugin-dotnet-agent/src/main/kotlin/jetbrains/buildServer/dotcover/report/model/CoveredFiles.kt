package jetbrains.buildServer.dotcover.report.model

import jetbrains.coverage.report.CoverageCodeRenderer
import java.util.Collections
import java.util.TreeMap

class CoveredFiles {

    private val _files: MutableMap<Int, CoveredFile> = TreeMap<Int, CoveredFile>()

    fun addLine(fileId: Int, line: Int, covered: Boolean) {
        var lineInfo: CoveredFile? = _files[fileId]

        if (lineInfo == null) {
            _files[fileId] = CoveredFile(fileId).also { lineInfo = it }
        }

        lineInfo?.addLine(line, covered)
    }

    val referredFiles: Set<Int>
        get() = Collections.unmodifiableSet(_files.keys)

    fun renderFile(sources: DotNetSourceCodeProvider, renderer: CoverageCodeRenderer) {
        for (file in _files.values) {
            file.renderFile(sources, renderer)
        }
    }
}
