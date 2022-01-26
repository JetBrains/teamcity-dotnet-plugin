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

import jetbrains.buildServer.util.FileUtil
import java.io.*

class FileSystemServiceImpl : FileSystemService {
    override fun read(file: File, reader: (InputStream) -> Unit) = FileInputStream(file).use(reader)

    override fun write(file: File, writer: (OutputStream) -> Unit) {
        FileUtil.createParentDirs(file)
        FileOutputStream(file).use(writer)
    }

    override fun list(file: File): Sequence<File> = file.listFiles()?.asSequence() ?: emptySequence()

    override fun isExists(file: File): Boolean = file.exists()

    override fun isFile(file: File): Boolean = file.isFile

    override fun copy(fileFrom: File, fileTo: File) = FileUtil.copy(fileFrom, fileTo)
}