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

package jetbrains.buildServer.agent

import jetbrains.buildServer.util.PEReader.PEUtil
import jetbrains.buildServer.util.PEReader.PEVersion
import java.io.File

class PEReaderImpl : PEReader {
    override fun tryGetVersion(file: File) =
        PEUtil.getProductVersion(file)?.let {
            Version(it.p1, it.p2, it.p3, it.p4)
        } ?: Version.Empty
}