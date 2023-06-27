using System.Text.RegularExpressions;
using Microsoft.Extensions.Logging;

namespace TeamCity.Dotnet.TestSuppressor.Domain.TestSelectors;

/// <summary>
/// Parses test query string into <see cref="ITestSelector"/> instance
/// Possible test query formats:
/// NamespaceA.NamespaceB.ClassName
/// NamespaceA.NamespaceB.NamespaceC.ClassName(param1,param2)
/// NamespaceA.ClassName(param1)
/// ...
/// </summary>
internal class TestSelectorParser : ITestSelectorParser
{
    private static readonly Regex Regex = new (@"^(?<namespaces>([^.]+\.)*)(?<class>[^(]+)(\((?<params>[^)]+)\))?$");
    private readonly ILogger<TestSelectorParser> _logger;

    public TestSelectorParser(ILogger<TestSelectorParser> logger)
    {
        _logger = logger;
    }
    
    public bool TryParseTestQuery(string testQuery, out ITestSelector? testSelector)
    {
        testSelector = null;
        
        _logger.LogDebug("Parsing test query: {TestQuery}", testQuery);
        if (string.IsNullOrWhiteSpace(testQuery))
        {
            _logger.LogWarning("Test query couldn't be empty: {TestQuery}", testQuery);
            return false;
        }

        var match = Regex.Match(testQuery);
        if (!HasNamespace(testQuery) || !match.Success)
        {
            _logger.LogWarning("Invalid test query format: {TestQuery}", testQuery);
            return false;
        }

        var namespaces = match.Groups["namespaces"].Value.Split('.').Where(s => !string.IsNullOrEmpty(s)).ToList();
        var className = match.Groups["class"].Value;
        
        testSelector = match.Groups["params"].Success
            ? new ParamTestClassSelector(namespaces, className, ParseParameters(match.Groups["params"].Value))
            : new TestClassSelector(namespaces, className);
        
        _logger.LogDebug("Test query successfully parsed: {TestQuery}", testQuery);
        
        return true;
    }

    private static List<string> ParseParameters(string paramString) =>
        paramString.Split(',').Select(p => p.Trim()).ToList();
    
    private static bool HasNamespace(string value) => value.Contains('.');
}

