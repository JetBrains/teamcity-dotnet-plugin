package jetbrains.buildServer.dotcover.report.model

import jetbrains.coverage.report.ClassInfo

interface ClassHolder {

    fun addClassInfo(info: ClassInfo)

    fun getCoveredFiles(): CoveredFiles
}
