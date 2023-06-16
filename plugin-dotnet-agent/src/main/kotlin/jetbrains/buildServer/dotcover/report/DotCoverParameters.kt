package jetbrains.buildServer.dotcover.report

import jetbrains.buildServer.dotnet.CoverageConstants

abstract class DotCoverParameters {

    protected abstract fun getValue(key: String): String?

    val dotCoverHomePath: String?
        get() = getValue(CoverageConstants.PARAM_DOTCOVER_HOME)

    fun present(): String {
        val path = dotCoverHomePath
        return "${CoverageConstants.PARAM_DOTCOVER_HOME}=${(path ?: "<null>")}"
    }

    companion object {
        fun equalParameters(p1: DotCoverParameters, p2: DotCoverParameters): Boolean {
            val f1 = p1.dotCoverHomePath
            val f2 = p2.dotCoverHomePath

            return if (f1 == null && f2 == null) true else f1 != null && f2 != null && f1 == f2
        }
    }
}
