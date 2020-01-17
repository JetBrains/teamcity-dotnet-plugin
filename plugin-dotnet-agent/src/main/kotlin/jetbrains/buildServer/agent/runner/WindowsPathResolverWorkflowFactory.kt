/*
 * Copyright 2000-2020 JetBrains s.r.o.
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

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.util.OSType
import java.io.File
import java.io.OutputStreamWriter

class WindowsPathResolverWorkflowFactory(
        private val _pathsService: PathsService,
        private val _fileSystemService: FileSystemService,
        private val _virtualContext: VirtualContext,
        private val _baseResolverWorkflowFactory: PathResolverWorkflowFactory)
    : PathResolverWorkflowFactory {
    override fun create(context: WorkflowContext, state: PathResolverState) =
                if (context.status == WorkflowStatus.Running && _virtualContext.targetOSType == OSType.WINDOWS) {
                    val command = Path(_pathsService.getTempFileName("where.cmd").path)
                    _fileSystemService.write(File(command.path)) {
                        OutputStreamWriter(it).use {
                            it.write(commandToResolve)
                        }
                    }

                    _baseResolverWorkflowFactory.create(context, PathResolverState(state.pathToResolve, state.virtualPathObserver, command))
                }
                else {
                    Workflow()
                }

    companion object {
        internal val commandToResolve = "@for %%A in (\"%path:;=\";\"%\") do @(@if exist %%~A\\%1 (@echo %%~A\\%1))"
    }
}