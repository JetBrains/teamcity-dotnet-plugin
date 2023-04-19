using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.TestsQueries;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Suppression;

internal class TestsSuppressionResolver : ITestsSuppressionResolver
{
    private readonly Dictionary<string, TestSuppressionCriterion> _testCriteriaRegistry;
    private readonly ILogger<TestsSuppressionResolver> _logger;
    private readonly ITestQueryParser _testQueryParser;

    public TestsSuppressionResolver(IOptions<Settings> options, ILogger<TestsSuppressionResolver> logger, ITestQueryParser testQueryParser)
    {
        _testCriteriaRegistry = new Dictionary<string, TestSuppressionCriterion>();
        _logger = logger;
        _testQueryParser = testQueryParser;

        var settings = options.Value;
        var testClassesFilePath = settings.TestClassesFilePath;

        if (string.IsNullOrEmpty(testClassesFilePath))
        {
            var currentDirectory = Directory.GetCurrentDirectory();
            var testFilePath = Path.Combine(currentDirectory, "tests.txt");

            if (File.Exists(testFilePath))
                testClassesFilePath = testFilePath;
            else
            {
                _logger.LogError("Tests file 'tests.txt' not found in the current directory.");
                throw new FileNotFoundException("Tests file 'tests.txt' not found in the current directory.");
            }
        }

        _logger.LogInformation($"Loading test criteria from file: {testClassesFilePath}");
        LoadTestCriteriaFromFile(testClassesFilePath, settings.InclusionMode);
    }

    public TestSuppressionCriterion ResolveCriteria(string testClassName)
    {
        var criterionFound = _testCriteriaRegistry.TryGetValue(testClassName, out var criterion);

        if (criterionFound)
        {
            _logger.LogInformation($"Test '{testClassName}' suppression criteria found: ShouldBeSuppressed = {criterion.ShouldBeSuppressed}");
        }
        else
        {
            _logger.LogInformation($"No suppression criteria found for test '{testClassName}'. Using default criteria: ShouldBeSuppressed = false");
            criterion = new TestSuppressionCriterion(false, new EmptyTestsQuery());
        }

        return criterion;
    }

    private void LoadTestCriteriaFromFile(string filePath, bool isInclusionMode)
    {
        if (!File.Exists(filePath))
        {
            _logger.LogError($"File '{filePath}' not found.");
            throw new FileNotFoundException($"File '{filePath}' not found.");
        }

        foreach (var line in File.ReadAllLines(filePath))
        {
            if (string.IsNullOrWhiteSpace(line) || line.TrimStart().StartsWith("#"))
                continue;

            var testQuery = _testQueryParser.ParseTestQuery(line.Trim());

            if (testQuery != null)
            {
                _testCriteriaRegistry[testQuery.Query] = new TestSuppressionCriterion(!isInclusionMode, testQuery);
            }
        }

        _logger.LogInformation($"Loaded {_testCriteriaRegistry.Count} test criteria from file.");
    }
}