package jetbrains.buildServer.dotcover

data class CoverageFilter(
        val type: CoverageFilterType,
        var defaultMask: String = Any,
        var moduleMask: String = Any,
        val classMask: String = Any,
        val functionMask: String = Any) {

    override fun toString(): String {
        return "$typeStr:$defaultMask;module=$moduleMask;class=$classMask;function=$functionMask"
    }

    private val typeStr: String
        get() = when (type) {
            CoverageFilter.CoverageFilterType.Undefined -> UNDEFINED_OP_SYMBOL
            CoverageFilter.CoverageFilterType.Include -> INCLUDE_SYMBOL
            CoverageFilter.CoverageFilterType.Exclude -> EXCLUDE_SYMBOL
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