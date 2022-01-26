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
package jetbrains.buildServer.agent

import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface FileSystemService {
    fun isExists(file: File): Boolean

    fun isDirectory(file: File): Boolean

    fun isFile(file: File): Boolean

    fun getLength(file: File): Long

    fun isAbsolute(file: File): Boolean

    fun write(file: File, writer: (OutputStream) -> Unit)

    fun read(file: File, reader: (InputStream) -> Unit)

    fun readBytes(file: File, operations: Sequence<FileReadOperation>): Sequence<FileReadOperationResult>

    fun copy(sourceDirectory: File, destinationDirectory: File)

    fun remove(fileOrDirectory: File): Boolean

    fun list(directory: File): Sequence<File>

    fun createDirectory(directory: File): Boolean

    fun sanitizeFileName(name: String): String

    fun generateTempFile(path: File, prefix: String, extension: String): File
}