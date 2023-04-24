namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;

internal enum Verbosity
{
    Quiet,      // no logs
    Minimal,    // errors only
    Normal,     // default (information logs)
    Detailed,   // 
    Diagnostic  // debug
}