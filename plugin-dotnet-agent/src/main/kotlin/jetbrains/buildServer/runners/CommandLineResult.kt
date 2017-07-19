package jetbrains.buildServer.runners

import org.apache.xpath.operations.Bool

data class CommandLineResult(
        private val _exitCode: Sequence<Int>,
        val standardOutput: Sequence<String>,
        val errorOutput: Sequence<String>) {

    val isCompleted: Boolean
        get() = _exitCode.any()

    val exitCode: Int
        get() = _exitCode.single()
}