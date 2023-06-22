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

package jetbrains.buildServer.dotnet.commands.targeting

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParameterType.Runner
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType.WorkingDirectory
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.CommandTarget
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_EXCLUDED_PATHS
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_PATHS
import java.io.File

class TargetServiceImpl(
        private val _pathsService: PathsService,
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService,
        private val _pathMatcher: PathMatcher,
        private val _fileSystemService: FileSystemService,
        private val _virtualContext: VirtualContext)
    : TargetService {
    override val targets: Sequence<CommandTarget>
        get() = sequence {
            val workingDirectory = _pathsService.getPath(WorkingDirectory)
            val includedTargets = resolveIncludedTargets(workingDirectory)
            val excludedTargets = resolveExcludedTargets(workingDirectory).toSet()

            includedTargets
                .filter { !excludedTargets.contains(it) }
                .forEach { yield(it) }
        }

    private fun resolveIncludedTargets(workingDirectory: File): Sequence<CommandTarget> = sequence {
        val includeRules = _parametersService.tryGetParameter(Runner, PARAM_PATHS)?.trim()
        if (includeRules.isNullOrEmpty())
            return@sequence

        _argumentsService.split(includeRules).forEach { includeRule ->
            if (wildCardPattern.matches(includeRule)) {
                resolvePathsOfThrow(workingDirectory, includeRule)
                    .forEach { yield(createCommandTarget(workingDirectory, it)) }
            } else yield(createCommandTarget(workingDirectory, File(includeRule)))
        }
    }

    private fun resolveExcludedTargets(workingDirectory: File): Sequence<CommandTarget> = sequence {
        val excludeRules = _parametersService.tryGetParameter(Runner, PARAM_EXCLUDED_PATHS)?.trim()
        if (excludeRules.isNullOrEmpty())
            return@sequence

        _argumentsService.split(excludeRules).forEach { excludeRule ->
            if (wildCardPattern.matches(excludeRule))
                _pathMatcher.match(workingDirectory, listOf(excludeRule)).forEach {
                    yield(createCommandTarget(workingDirectory, it))
                }
            else yield(createCommandTarget(workingDirectory, File(excludeRule)))
        }
    }

    private fun resolvePathsOfThrow(workingDirectory: File, pattern: String): List<File> {
        val targets = _pathMatcher.match(workingDirectory, listOf(pattern))
        if (targets.isEmpty()) {
            throw RunBuildException("Target paths not found for pattern \"$pattern\"")
        }
        return targets
    }

    private fun createCommandTarget(workingDirectory: File, target: File): CommandTarget {
        var targetFile: File = target
        if (!_fileSystemService.isAbsolute(target)) {
            targetFile = File(workingDirectory, target.path)
        }

        return CommandTarget(Path(_virtualContext.resolvePath(targetFile.path)))
    }

    companion object {
        val wildCardPattern = Regex(".*[?*].*")
    }
}