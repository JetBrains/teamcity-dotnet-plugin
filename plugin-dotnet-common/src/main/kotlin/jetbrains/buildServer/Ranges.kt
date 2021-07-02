package jetbrains.buildServer

infix fun <T: Comparable<T>>Bound<T>.to(that: Bound<T>): Range<T> = jetbrains.buildServer.SimpleRange<T>(this, that)
fun <T>combineOf(vararg ranges: Range<T>): Range<T> = MultiRange(*ranges)

private data class SimpleRange<T: Comparable<T>>(
        private val from: Bound<T>,
        private val to: Bound<T>)
    : Range<T> {
    override fun contains(value: T) =
            isAbove(value, from) && isLess(value, to)

    private fun isAbove(value: T, bound: Bound<T>) =
            if (bound.include) value.compareTo(bound.version) >= 0 else value.compareTo(bound.version) > 0

    private fun isLess(value: T, bound: Bound<T>) =
            if (bound.include) value.compareTo(bound.version) <= 0 else value.compareTo(bound.version) < 0

    override fun toString() =
        "${if(from.include) "[" else "("}$from, $to${if(to.include) "]" else ")"}"
}

private class MultiRange<T>(
        vararg val ranges: Range<T>)
    : Range<T> {
    override fun contains(value: T) =
            ranges.any { it.contains(value) }

    override fun toString() = ranges.map { it.toString() }.joinToString(", ")
}