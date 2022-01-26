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

import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface FileSystemService {
    fun read(file: File, reader: (InputStream) -> Unit)

    fun write(file: File, writer: (OutputStream) -> Unit)

    fun list(file: File): Sequence<File>

    fun isExists(file: File): Boolean

    fun isFile(file: File): Boolean

    fun copy(fileFrom: File, fileTo: File)
}