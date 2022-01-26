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

package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.dotnet.LoggerResolver
import jetbrains.buildServer.dotnet.ToolType
import java.io.File

class LoggerResolverStub(
        private val _msBuildLogger: File,
        private val _vstestLogger: File)
    : LoggerResolver {
    override fun resolve(toolType: ToolType) = when (toolType) {
        ToolType.MSBuild -> {
            _msBuildLogger
        }
        ToolType.VSTest -> {
            _vstestLogger
        }
        else -> {
            throw RunBuildException("Unknown tool $toolType")
        }
    }
}