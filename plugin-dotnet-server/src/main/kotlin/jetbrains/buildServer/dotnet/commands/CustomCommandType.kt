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
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.InvalidProperty
import org.springframework.beans.factory.BeanFactory

/**
 * Provides parameters for dotnet %custom% command.
 */
class CustomCommandType(
        private val _requirementFactory: RequirementFactory)
    : DotnetType(_requirementFactory) {
    override val name: String = DotnetCommandType.Custom.id

    override val description: String = "<custom>"

    override val editPage: String = "editCustomParameters.jsp"

    override val viewPage: String = "viewCustomParameters.jsp"

    override fun validateProperties(properties: Map<String, String>) = sequence {
        yieldAll(super.validateProperties(properties))

        if (properties[DotnetConstants.PARAM_PATHS].isNullOrBlank() && properties[DotnetConstants.PARAM_ARGUMENTS].isNullOrBlank()) {
            yield(InvalidProperty(DotnetConstants.PARAM_PATHS, VALIDATION_EMPTY))
            yield(InvalidProperty(DotnetConstants.PARAM_ARGUMENTS, VALIDATION_EMPTY))
        }
    }

    override fun getRequirements(parameters: Map<String, String>, factory: BeanFactory): Sequence<Requirement> = sequence {
        yieldAll(super.getRequirements(parameters, factory))
        
        if (parameters[DotnetConstants.PARAM_PATHS].isNullOrBlank()) {
            yieldAll(super.getRequirements(parameters, factory))
        }
    }

    companion object {
        const val VALIDATION_EMPTY: String = "Either the \"Executables\" field or \"Command line parameters\" should not be empty"
    }
}
