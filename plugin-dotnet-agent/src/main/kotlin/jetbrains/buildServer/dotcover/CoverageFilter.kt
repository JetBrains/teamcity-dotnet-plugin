package jetbrains.buildServer.dotcover

import com.intellij.openapi.util.text.StringUtil

data class CoverageFilter(
        val type: CoverageFilterType,
        var defaultMask: String = Any,
        var moduleMask: String = Any,
        val classMask: String = Any,
        val functionMask: String = Any) {

    override fun toString(): String {
        return typeStr + ":" + defaultMask + ";module=" + moduleMask + ";class=" + classMask + ";function=" + functionMask
    }

    private val typeStr: String
        get() {
            when (type) {
                CoverageFilter.CoverageFilterType.Undefined -> return UNDEFINED_OP_SYMBOL
                CoverageFilter.CoverageFilterType.Include -> return INCLUDE_SYMBOL
                CoverageFilter.CoverageFilterType.Exclude -> return EXCLUDE_SYMBOL
                else -> throw IllegalStateException()
            }
        }

    companion object {
        private val UNDEFINED_OP_SYMBOL = "?"
        private val EXCLUDE_SYMBOL = "-"
        private val INCLUDE_SYMBOL = "+"
        val Any = "*"
    }

    enum class CoverageFilterType {
        Undefined,
        Include,
        Exclude
    }
}