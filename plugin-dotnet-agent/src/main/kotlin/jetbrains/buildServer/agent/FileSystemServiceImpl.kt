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

import jetbrains.buildServer.util.FileUtil
import org.apache.commons.io.FileUtils
import java.io.*

class FileSystemServiceImpl: FileSystemService {
    override fun isExists(file: File): Boolean = file.exists()

    override fun isDirectory(file: File): Boolean = file.isDirectory

    override fun isFile(file: File): Boolean = file.isFile

    override fun getLength(file: File): Long = file.length()

    override fun isAbsolute(file: File): Boolean = file.isAbsolute

    override fun write(file: File, writer: (OutputStream) -> Unit) = FileOutputStream(file).use(writer)

    override fun read(file: File, reader: (InputStream) -> Unit) = FileInputStream(file).use(reader)

    override fun readBytes(file: File, operations: Sequence<FileReadOperation>) = sequence<FileReadOperationResult> {
        RandomAccessFile(file, "r").use {
            for (operation in operations) {
                it.seek(operation.fromPosition)
                yield(FileReadOperationResult(operation, it.read(operation.to)))
            }
        }
    }

    override fun copy(sourceDirectory: File, destinationDirectory: File) = FileUtils.copyDirectory(sourceDirectory, destinationDirectory)

    override fun remove(fileOrDirectory: File) = FileUtil.delete(fileOrDirectory)

    override fun list(directory: File): Sequence<File> = directory.listFiles()?.asSequence() ?: emptySequence()

    override fun createDirectory(directory: File) = directory.mkdirs()

    override fun sanitizeFileName(name: String) = FileUtil.sanitizeFileName(name)

    override fun generateTempFile(path: File, prefix: String, extension: String): File {
        createDirectory(path)
        var sequenceValue = 0L
        do {
            val fileName = sanitizeFileName(prefix + if (sequenceValue == 0L) "" else sequenceValue) + extension
            val file = File(path, fileName)
            if (!isExists(file) && !isDirectory(file)) {
                return file;
            }

            sequenceValue++
        } while (true)
    }
}