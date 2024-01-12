

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
            CoverageFilter.CoverageFilterType.Undefined -> UnspecifiedSymbol
            CoverageFilter.CoverageFilterType.Include -> IncludeSymbol
            CoverageFilter.CoverageFilterType.Exclude -> ExcludeSymbol
        }

    companion object {
        private const val UnspecifiedSymbol = "?"
        private const val ExcludeSymbol = "-"
        private const val IncludeSymbol = "+"
        internal const val Any = "*"
    }

    enum class CoverageFilterType {
        Undefined,
        Include,
        Exclude
    }
}