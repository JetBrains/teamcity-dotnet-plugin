using System.Text.RegularExpressions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

internal class TestSelectorParser : ITestSelectorParser
{
    private static readonly Regex _regex = new (@"^(?<suite>[^.]+)\.(?<class>[^(]+)(\((?<params>[^)]+)\))?$");
    
    public ITestsSelector? ParseTestQuery(string testQueryLine)
    {
        if (string.IsNullOrWhiteSpace(testQueryLine))
            return null;

        var match = _regex.Match(testQueryLine);

        if (!match.Success)
        {
            throw new ArgumentException($"Invalid test query format: {testQueryLine}");
        }

        var suiteName = match.Groups["suite"].Value;
        var className = match.Groups["class"].Value;

        return match.Groups["params"].Success
            ? new ParamTestClassSelector(suiteName, className, ParseParameters(match.Groups["params"].Value))
            : new TestClassSelector(suiteName, className) as ITestsSelector;
    }

    private static IList<string> ParseParameters(string paramString)
    {
        return paramString.Split(',').Select(p => p.Trim()).ToList();
    }
}
