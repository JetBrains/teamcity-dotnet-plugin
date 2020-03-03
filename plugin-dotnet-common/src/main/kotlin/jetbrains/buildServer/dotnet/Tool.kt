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

package jetbrains.buildServer.dotnet

enum class Tool(val version: Int, val type: ToolType, val platform: ToolPlatform, val bitness: ToolBitness, val description: String) {
    // Visual Studio
    VisualStudio2019(2019, ToolType.VisualStudio, ToolPlatform.Windows, ToolBitness.Any, "Visual Studio 2019"),
    VisualStudio2017(2017, ToolType.VisualStudio, ToolPlatform.Windows, ToolBitness.Any, "Visual Studio 2017"),
    VisualStudio2015(2015, ToolType.VisualStudio, ToolPlatform.Windows, ToolBitness.Any, "Visual Studio 2015"),
    VisualStudio2013(2013, ToolType.VisualStudio, ToolPlatform.Windows, ToolBitness.Any, "Visual Studio 2013"),
    VisualStudio2012(2012, ToolType.VisualStudio, ToolPlatform.Windows, ToolBitness.Any, "Visual Studio 2012"),
    VisualStudio2010(2010, ToolType.VisualStudio, ToolPlatform.Windows, ToolBitness.Any, "Visual Studio 2010"),

    // MSBuild
    MSBuildDotnetCore(0, ToolType.MSBuild, ToolPlatform.CrossPlatform, ToolBitness.Any, ".NET Core"),
    MSBuildMono(0, ToolType.MSBuild, ToolPlatform.Mono, ToolBitness.Any, "Mono"),

    MSBuild16WindowsX64(16, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X64, "16.0 64-bit"),
    MSBuild16WindowsX86(16, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X86, "16.0 32-bit"),

    MSBuild15WindowsX64(15, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X64, "15.0 64-bit"),
    MSBuild15WindowsX86(15, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X86, "15.0 32-bit"),

    MSBuild14WindowsX64(14, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X64, "14.0 64-bit"),
    MSBuild14WindowsX86(14, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X86, "14.0 32-bit"),

    MSBuild12WindowsX64(12, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X64, "12.0 64-bit"),
    MSBuild12WindowsX86(12, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X86, "12.0 32-bit"),

    // VSTest
    VSTestCrossPlatform(0, ToolType.VSTest, ToolPlatform.CrossPlatform, ToolBitness.Any, "Cross-platform"),
    VSTest16Windows(16, ToolType.VSTest, ToolPlatform.Windows, ToolBitness.Any, "16"),
    VSTest15Windows(15, ToolType.VSTest, ToolPlatform.Windows, ToolBitness.Any, "15"),
    VSTest14Windows(14, ToolType.VSTest, ToolPlatform.Windows, ToolBitness.Any, "14"),
    VSTest12Windows(12, ToolType.VSTest, ToolPlatform.Windows, ToolBitness.Any, "12");

    val id: String get() = "${type}_${version}_${platform}_$bitness"

    companion object {
        fun tryParse(id: String): Tool? {
            return Tool.values().singleOrNull { it.id.equals(id, true) }
        }
    }
}