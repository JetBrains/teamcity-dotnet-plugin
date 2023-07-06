package jetbrains.buildServer.dotcover.report.model

import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import jetbrains.coverage.report.ClassInfo
import jetbrains.coverage.report.CoverageCodeRenderer
import jetbrains.coverage.report.CoverageData
import jetbrains.coverage.report.CoverageSourceData
import java.io.File
import java.util.Collections

class DotCoverData(checkoutDir: File) : CoverageData, ClassHolder, CoverageSourceData {

    private val _filesMap: DotNetSourceCodeProvider
    private val _info: MutableList<ClassInfo>

    init {
        _filesMap = DotNetSourceCodeProvider(checkoutDir)
        _info = ArrayList()
    }

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

    fun preprocessFoundFiles(paramz: DotnetCoverageParameters) {
        _filesMap.preprocessFoundFiles(paramz, collectUsedFiles())
    }
}