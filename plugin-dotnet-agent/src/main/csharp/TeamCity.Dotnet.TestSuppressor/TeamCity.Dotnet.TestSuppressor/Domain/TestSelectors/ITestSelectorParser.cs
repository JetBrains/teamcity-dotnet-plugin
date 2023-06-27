namespace TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;

internal interface ITestSelectorParser
{
    bool TryParseTestQuery(string testQuery, out ITestSelector? testSelector);
}