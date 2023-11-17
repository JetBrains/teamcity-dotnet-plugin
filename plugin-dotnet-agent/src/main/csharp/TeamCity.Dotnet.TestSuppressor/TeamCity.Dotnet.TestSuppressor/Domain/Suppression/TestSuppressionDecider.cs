using TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Suppression;

internal class TestSuppressionDecider : ITestSuppressionDecider
{
    public (bool shouldBeSuppressed, TestSelector testSelector) Decide(string testSelectorQuery, bool inclusionMode, IReadOnlyDictionary<string, TestSelector> testSelectors)
    {
        if (string.IsNullOrWhiteSpace(testSelectorQuery))
        {
            throw new ArgumentException("Test selector query cannot be empty or null", nameof(testSelectorQuery));
        }

        // works only for test class selectors without parameters
        var (namespaces, className) = Parse(testSelectorQuery);

        return testSelectors.TryGetValue(testSelectorQuery, out var existingSelector)
            ? (shouldBeSuppressed: !inclusionMode, testSelector: existingSelector)
            : (shouldBeSuppressed: inclusionMode, testSelector: new TestSelector(namespaces, className));
    }

    private static (IList<string>, string) Parse(string testSelectorQuery)
    {
        var parenthesisIndex = testSelectorQuery.IndexOf('(');
        var querySegments = parenthesisIndex != -1
            ? testSelectorQuery[..parenthesisIndex].Split('.')
            : testSelectorQuery.Split('.');
        IList<string> namespaces = querySegments.Take(querySegments.Length - 1).ToList();
        return (namespaces, querySegments.Last());
    }
}
