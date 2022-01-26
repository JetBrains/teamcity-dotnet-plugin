/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

package jetbrains.buildServer.dotnet.discovery

import java.io.BufferedInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader

class ReaderFactoryImpl : ReaderFactory {
    override fun create(inputStream: InputStream): Reader = getInputStreamReader(BufferedInputStream(inputStream))

    private fun getInputStreamReader(inputStream: InputStream): Reader {
        inputStream.mark(3)
        val byte1 = inputStream.read()
        val byte2 = inputStream.read()
        return if (byte1 == 0xFF && byte2 == 0xFE) {
            InputStreamReader(inputStream, "UTF-16LE")
        } else if (byte1 == 0xFF && byte2 == 0xFF) {
            InputStreamReader(inputStream, "UTF-16BE")
        } else {
            val byte3 = inputStream.read()
            if (byte1 == 0xEF && byte2 == 0xBB && byte3 == 0xBF) {
                InputStreamReader(inputStream, "UTF-8")
            } else {
                inputStream.reset()
                InputStreamReader(inputStream)
            }
        }
    }
}