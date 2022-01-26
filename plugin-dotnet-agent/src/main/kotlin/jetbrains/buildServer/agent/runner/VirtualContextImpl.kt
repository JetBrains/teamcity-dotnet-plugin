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

package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.util.OSType
import java.io.File

class VirtualContextImpl(private val _buildStepContext: BuildStepContext): VirtualContext {
    private val _baseVirtaulContext
            get() = _buildStepContext.runnerContext.virtualContext

    override fun isVirtual() = _baseVirtaulContext.isVirtual

    override fun getTargetOSType(): OSType = _baseVirtaulContext.targetOSType

    override fun resolvePath(path: String): String {
        if (!isVirtual) {
           return path
        }

        try {
            var originalPath = File(path).canonicalPath
            val resolvedPath = _baseVirtaulContext.resolvePath(originalPath)
            if (originalPath == resolvedPath) {
                return path
            } else {
                return resolvedPath
            }
        } catch (ex: Throwable) {
            return path
        }
    }
}