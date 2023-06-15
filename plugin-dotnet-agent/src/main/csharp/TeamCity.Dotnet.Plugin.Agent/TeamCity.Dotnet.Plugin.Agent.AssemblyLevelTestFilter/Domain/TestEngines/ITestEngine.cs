namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;

internal interface ITestEngine
{
    public string Name { get; }
    
    public IEnumerable<string> AssembliesNames { get; }

    public IReadOnlyList<string> TestClassAttributes { get; }
    
    public IReadOnlyList<string> TestMethodAttributes { get; }
}