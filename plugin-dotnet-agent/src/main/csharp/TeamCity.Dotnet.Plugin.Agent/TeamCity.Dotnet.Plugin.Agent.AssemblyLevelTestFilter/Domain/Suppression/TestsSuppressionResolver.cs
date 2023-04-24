using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;


internal interface ITestSelectorsFactory
{
    Task<IReadOnlyDictionary<string, ITestsSelector>> LoadFromAsync(string filePath);
}


internal class TestSelectorsFactory : ITestSelectorsFactory
{
    private readonly IDictionary<string, ITestsSelector> _registry;
    private readonly ITestSelectorParser _testSelectorParser;
    private readonly ILogger<TestSelectorsFactory> _logger;

    public TestSelectorsFactory(ITestSelectorParser testSelectorParser, ILogger<TestSelectorsFactory> logger)
    {
        _registry = new Dictionary<string, ITestsSelector>();
        _testSelectorParser = testSelectorParser;
        _logger = logger;
    }

    public Task<IReadOnlyDictionary<string, ITestsSelector>> LoadFromAsync(string filePath)
    {
        if (string.IsNullOrEmpty(filePath))
        {
            var currentDirectory = Directory.GetCurrentDirectory();
            var testFilePath = Path.Combine(currentDirectory, "tests.txt");

            if (File.Exists(testFilePath))
                filePath = testFilePath;
            else
            {
                _logger.LogError("Tests file 'tests.txt' not found in the current directory");
                throw new FileNotFoundException("Tests file 'tests.txt' not found in the current directory");
            }
        }

        _logger.LogInformation("Loading test criteria from file: {TestClassesFilePath}", filePath);
        LoadTestCriteriaRegistryFromFile(filePath, inclusionMode);
    }

    public bool Get(string testName)
    {
        throw new NotImplementedException();
    }

    private IDictionary<string, TestSuppressionCriteria> LoadTestCriteriaRegistryFromFile(string filePath, bool isInclusionMode)
    {
        var registry = new Dictionary<string, TestSuppressionCriteria>();
        
        if (!File.Exists(filePath))
        {
            _logger.LogError("File {FilePath} not found", filePath);
            throw new FileNotFoundException($"File '{filePath}' not found");
        }

        foreach (var line in File.ReadAllLines(filePath))
        {
            if (string.IsNullOrWhiteSpace(line) || line.TrimStart().StartsWith("#"))
                continue;

            var testQuery = _testSelectorParser.ParseTestQuery(line.Trim());

            if (testQuery != null)
            {
                registry[testQuery.Query] = new TestSuppressionCriteria(!isInclusionMode, testQuery);
            }
        }

        _logger.LogInformation("Loaded {RegistryCount} test criteria from file", registry.Count);
    }
}

internal class TestSuppressionResolver : ITestsSuppressionResolver
{
    private readonly ITestSelectorsCollection _testSelectorsCollection;
    private readonly ILogger<TestSuppressionResolver> _logger;

    public TestSuppressionResolver(ITestSelectorsCollection testSelectorsCollection, ILogger<TestSuppressionResolver> logger)
    {
        _testSelectorsCollection = testSelectorsCollection;
        _logger = logger;
    }

    public TestSuppressionCriteria ResolveCriteria(string testClassName)
    {
        _testSelectorsCollection.Get(testClassName)   
        
        var criterionFound = _testCriteriaRegistry.TryGetValue(testClassName, out var criterion);

        if (criterionFound)
        {
            _logger.LogInformation("Test \'{TestClassName}\' suppression criteria found: ShouldBeSuppressed = {CriterionShouldBeSuppressed}", testClassName, criterion.ShouldBeSuppressed);
        }
        else
        {
            _logger.LogInformation("No suppression criteria found for test \'{TestClassName}\'. Using default criteria: ShouldBeSuppressed = false", testClassName);
            criterion = new TestSuppressionCriteria(false, new EmptyTestsSelector());
        }

        return criterion;
    }
}