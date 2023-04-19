namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.TestsQueries;

using System.Text.RegularExpressions;

internal class TestQueryParser : ITestQueryParser
{
    private static readonly Regex _regex = new (@"^(?<suite>[^.]+)\.(?<class>[^(]+)(\((?<params>[^)]+)\))?$");
    
    public ITestsQuery? ParseTestQuery(string testQueryLine)
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
            ? new ParamTestClassQuery(suiteName, className, ParseParameters(match.Groups["params"].Value))
            : new TestClassQuery(suiteName, className) as ITestsQuery;
    }

    private static IList<string> ParseParameters(string paramString)
    {
        return paramString.Split(',').Select(p => p.Trim()).ToList();
    }
}
