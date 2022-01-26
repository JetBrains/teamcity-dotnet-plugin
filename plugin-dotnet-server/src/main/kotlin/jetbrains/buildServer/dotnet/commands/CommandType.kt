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

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.RequirementFactory
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.serverSide.InvalidProperty
import org.springframework.beans.factory.BeanFactory

/**
 * Provides command-specific resources.
 */
abstract class CommandType(
        private val _requirementFactory: RequirementFactory) {
    abstract val name: String

    open val description: String
        get() = name

    abstract val editPage: String

    abstract val viewPage: String

    private fun getRequirements(parameters: Map<String, String>) =
            parameters[DotnetConstants.PARAM_REQUIRED_SDK]?.let {
                it
                        .split(" ", "\n", ";")
                        .asSequence()
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .map { RequirementResult(it, _requirementFactory.tryCreate(it)) }
            } ?: emptySequence()

    open fun validateProperties(properties: Map<String, String>): Sequence<InvalidProperty> =
            getRequirements(properties)
                    .filter { it.requirement == null }
                    .map { it }
                    .joinToString(",") { "\"${it.sdkVersion}\"" }
                    .let {
                        sequence {
                            if (it.isNotBlank()) {
                               yield(InvalidProperty(DotnetConstants.PARAM_REQUIRED_SDK, "Invalid version: $it"))
                            }
                        }
                    }

    open fun getRequirements(parameters: Map<String, String>, factory: BeanFactory) =
        getRequirements(parameters).mapNotNull { it.requirement }

    private data class RequirementResult(val sdkVersion: String, val requirement: Requirement?)
}
