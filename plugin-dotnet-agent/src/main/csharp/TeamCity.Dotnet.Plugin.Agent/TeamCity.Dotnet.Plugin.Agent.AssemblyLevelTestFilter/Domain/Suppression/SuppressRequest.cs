namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;

internal class SuppressRequest
{
    public string TestClassesFilePath { get; set; }
    
    public bool InclusionMode { get; set; }
}