using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter;

public class Settings
{
    /// <summary>
    /// Path to the assembly file to be patched
    /// </summary>
    [CommandLineOption("--assembly", "-a")]
    public IList<string> InputAssembliesPaths { get; set; }
    
    /// <summary>
    /// Path to the output patched assembly file
    /// </summary>
    [CommandLineOption("--output", "-o")]
    public string OutputAssembliesPath { get; set; }
    
    /// <summary>
    /// Path to the file containing the list of test classes to be removed from the input assembly
    /// </summary>
    [CommandLineOption("--test-classes-file-path", "-t")]
    public string? TestClassesFilePath { get; set; }
    
    /// <summary>
    /// How to handle test classes specified in the file
    /// </summary>
    [CommandLineOption("--inclusion-mode", "-m")]
    public bool InclusionMode { get; set; }
    
    /// <summary>
    /// Logging level (debug, info, warn, error)
    /// </summary>
    [CommandLineOption("--log-level", "-l")]
    public string LoggingLevel { get; set; }
}