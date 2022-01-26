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

package jetbrains.buildServer.dotnet.test.agent

import jetbrains.buildServer.agent.FileReadOperation
import jetbrains.buildServer.agent.FileReadOperationResult
import jetbrains.buildServer.agent.FileSystemService
import java.io.*

class VirtualFileSystemService : FileSystemService {
    private val _directories: MutableMap<File, DirectoryInfo> = mutableMapOf()
    private val _files: MutableMap<File, FileInfo> = mutableMapOf()

    override fun write(file: File, writer: (OutputStream) -> Unit) {
        addFile(file)
        writer(_files[file]!!.outputStream)
    }

    override fun read(file: File, reader: (InputStream) -> Unit) {
        reader(_files[file]!!.inputStream)
    }

    override fun readBytes(file: File, operations: Sequence<FileReadOperation>): Sequence<FileReadOperationResult> {
        TODO("Not yet implemented")
    }

    fun addDirectory(directory: File, attributes: Attributes = Attributes()): VirtualFileSystemService {
        _directories[directory] = DirectoryInfo(attributes)
        var parent: File? = directory
        while (parent != null) {
            parent = parent.parentFile
            if (parent != null) {
                if (!_directories.contains(parent)) {
                    _directories[parent] = DirectoryInfo(attributes)
                }
            }
        }

        return this
    }

    fun addFile(file: File, attributes: Attributes = Attributes(), inputStream: InputStream? = null, outputStream: OutputStream? = null): VirtualFileSystemService {
        val parent = file.parentFile
        if (parent != null) {
            addDirectory(parent)
        }

        if (!_files.containsKey(file)) {
            val curOutputStream = outputStream ?: PipedOutputStream()
            val curInputStream = inputStream ?: PipedInputStream(curOutputStream as PipedOutputStream)
            _files[file] = FileInfo(attributes, curInputStream, curOutputStream, inputStream == null)
        }

        return this
    }

    override fun isExists(file: File): Boolean = _directories.contains(file) || _files.contains(file)

    override fun isDirectory(file: File): Boolean = _directories.contains(file)

    override fun isFile(file: File): Boolean = _files.contains(file)

    override fun getLength(file: File): Long =
        _files[file]!!.let{
            if (it.isEmpty) {
                0;
            }
            else {
                it.inputStream.readBytes().size.toLong()
            }
        }

    override fun isAbsolute(file: File): Boolean = _directories[file]?.attributes?.isAbsolute ?: _files[file]?.attributes?.isAbsolute ?: false

    override fun copy(sourceDirectory: File, destinationDirectory: File) {
        if (!isDirectory(sourceDirectory)) {
            val sourceFile = _files[sourceDirectory]!!
            addFile(destinationDirectory, sourceFile.attributes)
            _files[destinationDirectory] = sourceFile
        } else {
            val sourceDir = _directories[sourceDirectory]!!
            addDirectory(destinationDirectory, sourceDir.attributes)
            _directories[destinationDirectory] = sourceDir
        }
    }

    override fun remove(fileOrDirectory: File): Boolean {
        val fileInfo = _files[fileOrDirectory]
        if (fileInfo != null) {
            val errorOnRemove = fileInfo.attributes.errorOnRemove
            if (errorOnRemove != null) {
                throw errorOnRemove
            }

            _files.remove(fileOrDirectory)
            return true
        }

        val dirInfo = _directories[fileOrDirectory]
        if (dirInfo != null) {
            val errorOnRemove = dirInfo.attributes.errorOnRemove
            if (errorOnRemove != null) {
                throw errorOnRemove
            }

            _directories.remove(fileOrDirectory)
            return true
        }

        return false
    }

    override fun list(directory: File): Sequence<File> = _directories.keys.asSequence().plus(_files.map { it.key }).filter { it.parentFile == directory }

    override fun createDirectory(directory: File): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sanitizeFileName(name: String) = name

    override fun generateTempFile(path: File, prefix: String, extension: String) = File(path, "${prefix}99${extension}")

    private data class FileInfo(val attributes: Attributes, val inputStream: InputStream, val outputStream: OutputStream, val isEmpty: Boolean)

    private data class DirectoryInfo(val attributes: Attributes)

    data class Attributes(var isAbsolute: Boolean = false, var errorOnRemove: Exception? = null)

    companion object {
        fun absolute(isAbsolute: Boolean = true): Attributes {
            val attr = Attributes()
            attr.isAbsolute = isAbsolute
            return attr
        }

        fun errorOnRemove(errorOnRemove: Exception): Attributes {
            val attr = Attributes()
            attr.errorOnRemove = errorOnRemove
            return attr
        }
    }
}