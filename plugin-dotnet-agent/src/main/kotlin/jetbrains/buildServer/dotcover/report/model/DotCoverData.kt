package jetbrains.buildServer.dotcover.report.model

import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.coverage.report.ClassInfo
import jetbrains.coverage.report.CoverageCodeRenderer
import jetbrains.coverage.report.CoverageData
import jetbrains.coverage.report.CoverageSourceData
import java.io.File
import java.util.Collections

class DotCoverData(checkoutDir: File) : CoverageData, ClassHolder, CoverageSourceData {

    private val _filesMap: DotNetSourceCodeProvider = DotNetSourceCodeProvider(checkoutDir)
    private val _info: MutableList<ClassInfo> = ArrayList()

    val filesMap: DotNetSourceCodeProvider get() = _filesMap

    override fun getClasses(): Collection<ClassInfo> {
        return Collections.unmodifiableCollection(_info)
    }

    override fun getCoveredFiles(): CoveredFiles {
        return CoveredFiles()
    }

    override fun addClassInfo(info: ClassInfo) {
        _info.add(info)
    }

    override fun getSourceData(): CoverageSourceData? {
        return this
    }

    override fun renderSourceCodeFor(clazz: ClassInfo, renderer: CoverageCodeRenderer) {
        (clazz as DotCoverClass).getCoveredFiles().renderFile(_filesMap, renderer)
    }

    private fun collectUsedFiles(): Set<Int> {
        val result: MutableSet<Int> = HashSet()
        for (info in _info) {
            result.addAll((info as DotCoverClass).getCoveredFiles().referredFiles)
        }
        return result
    }

    fun preprocessFoundFiles(buildLogger: BuildProgressLogger,
                             configParameters: Map<String, String>) {
        _filesMap.preprocessFoundFiles(buildLogger, configParameters, collectUsedFiles())
    }
}
