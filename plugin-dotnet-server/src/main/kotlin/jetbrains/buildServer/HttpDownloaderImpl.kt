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

package jetbrains.buildServer

import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class HttpDownloaderImpl : HttpDownloader {
    override fun download(url: URL, outputStream: OutputStream) {
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.inputStream.use {
                val buffer = ByteArray(1024)
                var count: Int
                do {
                    count = it.read(buffer)
                    if (count > 0) {
                        outputStream.write(buffer, 0, count)
                    }
                } while (count > 0)

                outputStream.flush()
            }
        } finally {
            connection.disconnect()
        }
    }
}