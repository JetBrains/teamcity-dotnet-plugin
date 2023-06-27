namespace TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;

internal interface ITestSelector
{
    string Query { get; }
}
