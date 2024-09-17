package jetbrains.buildServer.agent

enum class TargetType(val priority: Int) { // Examples:
    NotApplicable(0),
    SystemDiagnostics(1),           // e.g. `dotnet --version` to determine .NET SDK
    AuxiliaryTool(50),              // tools that help to prepare environment, e.g. TeamCity.Dotnet.TestSuppressor
    Tool(100),                      // dotnet, msbuild, nuget, etc
    ToolHost(200),                  // dotnet, mono, etc – in case of tool target can't be executed directly
    PerformanceProfiler(300),       // dotTrace, etc
    MemoryProfiler(310),            // dotMemory, etc
    CodeCoverageProfiler(320),      // dotCover.exe / dotCover.sh / dotCover.dll, etc
    ProfilerHost(400),              // dotnet, mono, etc – in case of profiler target can't be executed directly
}