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

package jetbrains.buildServer.agent

enum class TargetType(val priority: Int) { // Examples:
    NotApplicable(0),
    SystemDiagnostics(1),           // e.g. `dotnet --version` to determine .NET SDK
    AuxiliaryTool(50),
    Tool(100),                      // dotnet, msbuild, nuget, etc
    ToolHost(200),                  // dotnet, mono, etc – in case of tool target can't be executed directly
    PerformanceProfiler(300),       // dotTrace, etc
    MemoryProfiler(310),            // dotMemory, etc
    CodeCoverageProfiler(320),      // dotCover.exe / dotCover.sh / dotCover.dll, etc
    ProfilerHost(400),              // dotnet, mono, etc – in case of profiler target can't be executed directly
}