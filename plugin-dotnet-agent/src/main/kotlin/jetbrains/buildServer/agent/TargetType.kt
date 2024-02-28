package jetbrains.buildServer.agent

enum class TargetType(val priority: Int) { // Examples:
    NotApplicable(0),
    SystemDiagnostics(100),  // e.g. `dotnet --version` to determine .NET SDK
    AuxiliaryTool(200),      // tools that help to prepare environment, e.g. TeamCity.Dotnet.TestSuppressor
    Tool(300),               // dotnet, msbuild, nuget, etc
    PostProcessing(400),     // workflows that should be applied after the main tool
}

enum class CommandLineLayer(val priority: Int) {  // Examples:
    Tool(100),                             // dotnet, msbuild, nuget, etc
    ToolHost(200),                         // dotnet, mono, etc – in case of tool target can't be executed directly
    Profiler(300),                         // dotCover.exe / dotCover.sh / dotCover.dll, etc
    ProfilerHost(400),                     // dotnet, mono, etc – in case of profiler target can't be executed directly

    companion object {
        infix fun CommandLineLayer.within(range: Pair<CommandLineLayer, CommandLineLayer>): Boolean =
            this.priority >= range.first.priority && this.priority <= range.second.priority
    }
}