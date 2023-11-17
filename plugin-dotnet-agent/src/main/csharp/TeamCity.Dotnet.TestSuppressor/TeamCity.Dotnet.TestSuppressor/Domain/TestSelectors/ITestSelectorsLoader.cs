namespace TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;

internal interface ITestSelectorsLoader
{
    Task<IReadOnlyDictionary<string, TestSelector>> LoadTestSelectorsFromAsync(string filePath);
}