namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;

internal interface ITestEngine
{
    public string Name { get; }
    
    public IList<string> TestClassAttributes { get; }
    
    public IList<string> TestMethodAttributes { get; }
}