/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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