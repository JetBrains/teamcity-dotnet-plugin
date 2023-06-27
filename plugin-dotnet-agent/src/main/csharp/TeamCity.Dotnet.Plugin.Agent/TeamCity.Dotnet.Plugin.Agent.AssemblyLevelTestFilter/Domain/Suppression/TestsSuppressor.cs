using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;

internal class TestsSuppressor : ITestsSuppressor
{
    private readonly IReadOnlyDictionary<(Type, Type), ITestSuppressingStrategy> _suppressingStrategies;
    private readonly ILogger<TestsSuppressor> _logger;

    public TestsSuppressor(IEnumerable<ITestSuppressingStrategy> suppressingStrategies, ILogger<TestsSuppressor> logger)
    {
        _suppressingStrategies = suppressingStrategies.ToDictionary(
            strategy => (strategy.TestEngineType, strategy.TestSelectorType),
            strategy => strategy
        );
        _logger = logger;
        _logger.LogDebug("TestsSuppressor initialized with {StrategyCount} suppressing strategies", _suppressingStrategies.Count);
    }
    
    public TestSuppressionResult SuppressTests(IDotnetType testClass, TestSuppressionParameters parameters)
    {
        _logger.LogDebug(
            "Suppressing tests for {TestClass} with {TestEngine} engine by selector: {TestSelector}",
            testClass.FullName, parameters.TestEngine.GetType(), parameters.TestSelector.Query
        );
        
        try
        {
            var suppressionStrategy = ResolveSuppressingStrategy(parameters.TestEngine, parameters.TestSelector);
            var suppressionResult = suppressionStrategy.SuppressTests(testClass, parameters.TestSelector);
            _logger.LogDebug("Tests suppressed successfully for {TestClass}", testClass.FullName);
            return suppressionResult;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "An error occurred while suppressing tests for {TestClass}", testClass.FullName);
            throw;
        }
    }
    
    private ITestSuppressingStrategy ResolveSuppressingStrategy(ITestEngine testEngine, ITestSelector testsQuery)
    {
        var key = (testEngine.GetType(), testsQuery.GetType());
        if (_suppressingStrategies.TryGetValue(key, out var suppressingStrategy))
        {
            _logger.LogDebug("Suppressing strategy found for {TestEngineType} and {TestsQueryType}", key.Item1, key.Item2);
            return suppressingStrategy;
        }
        
        _logger.LogError("No suppressing strategy found for {TestEngineType} and {TestsQueryType}", key.Item1, key.Item2);
        throw new InvalidOperationException($"No suppressing strategy found for the given test engine and tests query combination: {key}");
    }
}
