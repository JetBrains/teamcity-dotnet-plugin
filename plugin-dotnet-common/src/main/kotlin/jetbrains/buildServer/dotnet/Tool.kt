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

package jetbrains.buildServer.dotnet

enum class Tool(val version: Int, val type: ToolType, val platform: ToolPlatform, val bitness: ToolBitness, val description: String, val vsVersion: Int = 0) {
    // Visual Studio
    VisualStudioAny(0, ToolType.VisualStudio, ToolPlatform.Windows, ToolBitness.Any, ToolBitness.Any.description, 0),
    VisualStudio2022(17, ToolType.VisualStudio, ToolPlatform.Windows, ToolBitness.Any, "${ToolType.VisualStudio.description} 2022", 2022),
    VisualStudio2019(16, ToolType.VisualStudio, ToolPlatform.Windows, ToolBitness.Any, "${ToolType.VisualStudio.description} 2019", 2019),
    VisualStudio2017(15, ToolType.VisualStudio, ToolPlatform.Windows, ToolBitness.Any, "${ToolType.VisualStudio.description} 2017", 2017),
    VisualStudio2015(14, ToolType.VisualStudio, ToolPlatform.Windows, ToolBitness.Any, "${ToolType.VisualStudio.description} 2015", 2015),
    VisualStudio2013(12, ToolType.VisualStudio, ToolPlatform.Windows, ToolBitness.Any, "${ToolType.VisualStudio.description} 2013", 2013),
    VisualStudio2012(11, ToolType.VisualStudio, ToolPlatform.Windows, ToolBitness.Any, "${ToolType.VisualStudio.description} 2012", 2012),
    VisualStudio2010(10, ToolType.VisualStudio, ToolPlatform.Windows, ToolBitness.Any, "${ToolType.VisualStudio.description} 2010", 2010),

    // MSBuild
    MSBuildCrossPlatform(0, ToolType.MSBuild, ToolPlatform.CrossPlatform, ToolBitness.Any, "${ToolPlatform.CrossPlatform.description} ${ToolType.MSBuild.description}"),
    MSBuildMono(0, ToolType.MSBuild, ToolPlatform.Mono, ToolBitness.Any, "${ToolPlatform.Mono.description} ${ToolType.MSBuild.description}"),

    MSBuild17Windows(17, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.Any, "${ToolType.MSBuild.description} Tools 2022", 2022),
    MSBuild17WindowsX64(17, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X64, "${ToolType.MSBuild.description} Tools 2022 ${ToolBitness.X64.description}", 2022),
    MSBuild17WindowsX86(17, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X86, "${ToolType.MSBuild.description} Tools 2022 ${ToolBitness.X86.description}", 2022),

    MSBuild16Windows(16, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.Any, "${ToolType.MSBuild.description} Tools 2019", 2019),
    MSBuild16WindowsX64(16, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X64, "${ToolType.MSBuild.description} Tools 2019 ${ToolBitness.X64.description}", 2019),
    MSBuild16WindowsX86(16, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X86, "${ToolType.MSBuild.description} Tools 2019 ${ToolBitness.X86.description}", 2019),

    MSBuild15Windows(15, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.Any, "${ToolType.MSBuild.description} Tools 2017", 2017),
    MSBuild15WindowsX64(15, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X64, "${ToolType.MSBuild.description} Tools 2017 ${ToolBitness.X64.description}", 2017),
    MSBuild15WindowsX86(15, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X86, "${ToolType.MSBuild.description} Tools 2017 ${ToolBitness.X86.description}", 2017),

    MSBuild14Windows(14, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.Any, "${ToolType.MSBuild.description} Tools 2015", 2015),
    MSBuild14WindowsX64(14, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X64, "${ToolType.MSBuild.description} Tools 2015 ${ToolBitness.X64.description}", 2015),
    MSBuild14WindowsX86(14, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X86, "${ToolType.MSBuild.description} Tools 2015 ${ToolBitness.X86.description}", 2015),

    MSBuild12Windows(12, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.Any, "${ToolType.MSBuild.description} Tools 2013", 2013),
    MSBuild12WindowsX64(12, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X64, "${ToolType.MSBuild.description} Tools 2013 ${ToolBitness.X64.description}", 2013),
    MSBuild12WindowsX86(12, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X86, "${ToolType.MSBuild.description} Tools 2013 ${ToolBitness.X86.description}", 2013),

    MSBuild4Windows(4, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.Any, "${ToolType.MSBuild.description} 4.0", 2010),
    MSBuild4WindowsX64(4, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X64, "${ToolType.MSBuild.description} 4.0 ${ToolBitness.X64.description}", 2010),
    MSBuild4WindowsX86(4, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X86, "${ToolType.MSBuild.description} 4.0 ${ToolBitness.X86.description}", 2010),

    // VSTest
    VSTestCrossPlatform(0, ToolType.VSTest, ToolPlatform.CrossPlatform, ToolBitness.Any, "${ToolPlatform.CrossPlatform.description} ${ToolType.VSTest.description}"),
    VSTest17Windows(17, ToolType.VSTest, ToolPlatform.Windows, ToolBitness.Any, "${ToolType.VSTest.description} 2022", 2022),
    VSTest16Windows(16, ToolType.VSTest, ToolPlatform.Windows, ToolBitness.Any, "${ToolType.VSTest.description} 2019", 2019),
    VSTest15Windows(15, ToolType.VSTest, ToolPlatform.Windows, ToolBitness.Any, "${ToolType.VSTest.description} 2017", 2017),
    VSTest14Windows(14, ToolType.VSTest, ToolPlatform.Windows, ToolBitness.Any, "${ToolType.VSTest.description} 2015", 2015),
    VSTest12Windows(12, ToolType.VSTest, ToolPlatform.Windows, ToolBitness.Any, "${ToolType.VSTest.description} 2013", 2013);

    val id: String = "${type}${if (version != 0) "_${version}" else ""}_${platform}${if (bitness != ToolBitness.Any) "_${bitness}" else ""}"

    companion object {
        fun tryParse(id: String): Tool? {
            return Tool.values().singleOrNull { it.id.equals(id, true) }
        }
    }
}