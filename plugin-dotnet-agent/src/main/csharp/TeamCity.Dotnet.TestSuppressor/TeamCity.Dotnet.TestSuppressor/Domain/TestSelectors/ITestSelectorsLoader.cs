namespace TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;

internal interface ITestSelectorsLoader
{
    Task<IReadOnlyDictionary<string, ITestSelector>> LoadTestSelectorsFromAsync(string filePath);
}