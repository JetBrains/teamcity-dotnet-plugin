namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.TestEngines;

internal interface ITestEngine
{
    public string Name { get; }
    
    public IList<string> TestClassAttributes { get; }
    
    public IList<string> TestMethodAttributes { get; }
}