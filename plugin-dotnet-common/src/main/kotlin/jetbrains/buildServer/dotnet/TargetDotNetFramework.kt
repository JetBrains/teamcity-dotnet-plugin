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

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType

public enum class TargetDotNetFramework(val version: String) {
    v2_0("2.0"),
    v3_0("3.0"),
    v3_5("3.5"),
    v4_0("4.0"),
    v4_5("4.5"),
    v4_5_1("4.5.1"),
    v4_5_2("4.5.2"),
    v4_6("4.6"),
    v4_6_1("4.6.1"),
    v4_6_2("4.6.2"),
    v4_7("4.7"),
    v4_7_1("4.7.1"),
    v4_7_2("4.7.2"),
    v4_8("4.8"),
    v4_8_1("4.8.1");
    // Don't forget to update DSL while adding new version

    val id: String get() = "TargetDotNetFramework_$version"
    val description: String get() = ".NET Framework $version"
    val propertyName: String get() = "DotNetFrameworkTargetingPack$version"

    fun createExistsRequirement(): Requirement =
            Requirement(RequirementQualifier.EXISTS_QUALIFIER + propertyName + "_.*", null, RequirementType.EXISTS)
}