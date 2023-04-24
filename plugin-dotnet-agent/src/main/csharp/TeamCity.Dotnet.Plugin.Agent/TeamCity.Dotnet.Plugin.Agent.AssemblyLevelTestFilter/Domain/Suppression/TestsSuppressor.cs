using Microsoft.Extensions.Logging;
using Mono.Cecil;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;

internal class TestsSuppressor : ITestsSuppressor
{
    private readonly IDictionary<(Type, Type), ITestSuppressingStrategy> _suppressingStrategies;
    private readonly ILogger<TestsSuppressor> _logger;

    public TestsSuppressor(IEnumerable<ITestSuppressingStrategy> suppressingStrategies, ILogger<TestsSuppressor> logger)
    {
        _suppressingStrategies = suppressingStrategies.ToDictionary(
            strategy => GetStrategyTypeParameters(strategy.GetType()),
            strategy => strategy
        );
        _logger = logger;
        _logger.LogDebug("TestsSuppressor initialized with {StrategyCount} suppressing strategies", _suppressingStrategies.Count);
    }
    
    public int SuppressTests(TypeDefinition testClass, ITestEngine testEngine, TestSuppressionCriteria suppressionCriteria)
    {
        if (!suppressionCriteria.ShouldBeSuppressed)
        {
            _logger.LogDebug("SuppressTests called, but suppression criterion indicates no suppression is needed");
            return 0;
        }
        
        try
        {
            var suppressionStrategy = ResolveSuppressingStrategy(testEngine, suppressionCriteria.TestsSelector);
            suppressionStrategy.SuppressTests(testClass, suppressionCriteria.TestsSelector);
            _logger.LogInformation("Tests suppressed successfully for {TestClass}", testClass.FullName);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "An error occurred while suppressing tests for {TestClass}", testClass.FullName);
            throw;
        }
    }
    
    private ITestSuppressingStrategy<TEngine, TTestsQuery> ResolveSuppressingStrategy<TEngine, TTestsQuery>(TEngine testEngine, TTestsQuery testsQuery)
        where TEngine : ITestEngine
        where TTestsQuery : ITestsSelector
    {
        var key = (testEngine.GetType(), testsQuery.GetType());
        
        if (_suppressingStrategies.TryGetValue(key, out var suppressingStrategy))
        {
            _logger.LogDebug("Suppressing strategy found for {TestEngineType} and {TestsQueryType}", key.Item1, key.Item2);
            return (ITestSuppressingStrategy<TEngine, TTestsQuery>) suppressingStrategy;
        }
        
        _logger.LogError("No suppressing strategy found for {TestEngineType} and {TestsQueryType}", key.Item1, key.Item2);
        throw new InvalidOperationException($"No suppressing strategy found for the given test engine and tests query combination: {key}");
    }

    private static (Type, Type) GetStrategyTypeParameters(Type strategyType)
    {
        var genericStrategyInterface = strategyType.GetInterfaces()
            .FirstOrDefault(i => i.IsGenericType && i.GetGenericTypeDefinition() == typeof(ITestSuppressingStrategy<,>));

        if (genericStrategyInterface == null)
        {
            throw new InvalidOperationException($"The strategy type {strategyType} does not implement ITestSuppressingStrategy<TTestEngine, TTestsQuery>");
        }

        var typeParameters = genericStrategyInterface.GetGenericArguments();
        return (typeParameters[0], typeParameters[1]);
    }
}
