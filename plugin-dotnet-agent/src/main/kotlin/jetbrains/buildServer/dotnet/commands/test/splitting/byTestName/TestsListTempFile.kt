

package jetbrains.buildServer.dotnet.commands.test.splitting.byTestName

import jetbrains.buildServer.utils.getBufferedReader
import jetbrains.buildServer.utils.getBufferedWriter
import java.io.BufferedWriter
import java.io.File

class TestsListTempFile(private val _file: File) : TestsList {
    private var _testsCounter = 0
    private var _testsListFileWriter: BufferedWriter? = null

    override val testsCount: Int get() = _testsCounter

    override val tests: Sequence<String> get() = sequence {
        closeWriter()       // reading while we have opened writer could lead to lost data
        _file.getBufferedReader()
            .use {
                while (it.ready())
                    yield(it.readLine())
            }
    }

    override fun add(testName: String) {
        val writer = if (_testsListFileWriter == null) {
            _testsListFileWriter = _file.getBufferedWriter()
            _testsListFileWriter as BufferedWriter
        } else _testsListFileWriter as BufferedWriter

        writer.write(testName)
        writer.newLine()
        _testsCounter++
    }

    override fun dispose() {
        closeWriter()
        _testsCounter = 0
    }

    private fun closeWriter() {
        _testsListFileWriter?.close()
        _testsListFileWriter = null
    }
}