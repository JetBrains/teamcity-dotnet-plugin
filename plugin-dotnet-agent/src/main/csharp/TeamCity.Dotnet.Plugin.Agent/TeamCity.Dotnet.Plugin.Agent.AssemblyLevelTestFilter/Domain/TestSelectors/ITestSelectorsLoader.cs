namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

internal interface ITestSelectorsLoader
{
    Task<IReadOnlyDictionary<string, ITestSelector>> LoadTestSelectorsFromAsync(string filePath);
}