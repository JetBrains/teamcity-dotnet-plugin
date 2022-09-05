package jetbrains.buildServer.dotnet.commands.test.splitTests

import java.io.BufferedWriter
import java.io.File

class TestsListTempFile(private val _file: File) : TestsList {
    private var _testsCounter = 0
    private var _testsListFileWriter: BufferedWriter? = null

    override val testsCount: Int get() = _testsCounter

    override val tests: Sequence<String> get() = sequence {
        _file.bufferedReader()
            .use {
                while (it.ready())
                    yield(it.readLine())
            }
    }

    override fun add(testName: String) {
        var writer = if (_testsListFileWriter == null) {
            _testsListFileWriter = _file.bufferedWriter()
            _testsListFileWriter as BufferedWriter
        } else _testsListFileWriter as BufferedWriter

        writer.appendLine(testName)
        _testsCounter++
    }

    override fun dispose() {
        _testsListFileWriter?.close()
        _testsCounter = 0
        _testsListFileWriter = null
    }
}