package jetbrains.buildServer.dotcover.report.model

import jetbrains.coverage.report.Entry

class AdditiveValue {
    private var _total = 0
    private var _covered = 0

    val entry: Entry
        get() = Entry(_total, _covered)

    fun increment(total: Int, covered: Int) {
        _total += total
        _covered += covered
    }
}
