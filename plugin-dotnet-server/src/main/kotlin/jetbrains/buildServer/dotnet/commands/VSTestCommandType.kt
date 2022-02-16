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

import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.InvalidProperty
import org.springframework.beans.factory.BeanFactory

/**
 * Provides parameters for dotnet VSTest command.
 */
class VSTestCommandType(
        private val _requirementFactory: RequirementFactory)
    : CommandType(_requirementFactory) {
    override val name: String = DotnetCommandType.VSTest.id

    override val editPage: String = "editVSTestParameters.jsp"

    override val viewPage: String = "viewVSTestParameters.jsp"

    override fun getRequirements(parameters: Map<String, String>, factory: BeanFactory) = sequence {
        yieldAll(super.getRequirements(parameters, factory))

        var shouldBeWindows = false
        var hasRequirement = false
        parameters[DotnetConstants.PARAM_VSTEST_VERSION]?.let {
            Tool.tryParse(it)?.let {
                if (it.type == ToolType.VSTest) {
                    when (it.platform) {
                        ToolPlatform.Windows -> {
                            yield(Requirement("teamcity.dotnet.vstest.${it.version}.0", null, RequirementType.EXISTS))
                            shouldBeWindows = true
                            hasRequirement = true
                        }
                        else -> { }
                    }
                }
            }
        }

        if (!hasRequirement) {
            yield(Requirement(DotnetConstants.CONFIG_SUFFIX_DOTNET_CLI_PATH, null, RequirementType.EXISTS))
        }

        if (shouldBeWindows) {
            yield(Requirement("teamcity.agent.jvm.os.name", "Windows", RequirementType.STARTS_WITH))
        }
    }

    override fun validateProperties(properties: Map<String, String>) = sequence {
        yieldAll(super.validateProperties(properties))

        DotnetConstants.PARAM_PATHS.let {
            if (properties[it].isNullOrBlank()) {
                yield(InvalidProperty(it, DotnetConstants.VALIDATION_EMPTY))
            }
        }

        when (properties[DotnetConstants.PARAM_TEST_FILTER]) {
            "name" -> {
                DotnetConstants.PARAM_TEST_NAMES.let {
                    if (properties[it].isNullOrBlank()) {
                        yield(InvalidProperty(it, DotnetConstants.VALIDATION_EMPTY))
                    }
                }
            }
            "filter" -> {
                DotnetConstants.PARAM_TEST_CASE_FILTER.let {
                    if (properties[it].isNullOrBlank()) {
                        yield(InvalidProperty(it, DotnetConstants.VALIDATION_EMPTY))
                    }
                }
            }
        }
    }
}