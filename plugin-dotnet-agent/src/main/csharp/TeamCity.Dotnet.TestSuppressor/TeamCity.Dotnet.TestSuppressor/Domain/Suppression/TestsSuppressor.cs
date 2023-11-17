using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.TestSuppressor.Domain.TestEngines;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Suppression;

internal class TestsSuppressor : ITestsSuppressor
{
    private readonly IReadOnlyDictionary<Type, ITestSuppressionStrategy> _suppressionStrategies;
    private readonly ILogger<TestsSuppressor> _logger;

    public TestsSuppressor(IEnumerable<ITestSuppressionStrategy> suppressionStrategies, ILogger<TestsSuppressor> logger)
    {
        _suppressionStrategies = suppressionStrategies.ToDictionary(
            strategy => strategy.TestEngineType,
            strategy => strategy
        );
        _logger = logger;
        _logger.LogDebug("TestsSuppressor initialized with {StrategyCount} suppression strategies", _suppressionStrategies.Count);
    }
    
    public TestSuppressionResult SuppressTests(IDotnetType testClass, TestSuppressionParameters parameters)
    {
        _logger.LogDebug(
            "Tests suppression for {TestClass} with {TestEngine} engine by selector: {TestSelector}",
            testClass.FullName, parameters.TestEngine.GetType(), parameters.TestSelector.Query
        );
        
        try
        {
            var suppressionStrategy = ResolveSuppressionStrategy(parameters.TestEngine);
            var suppressionResult = suppressionStrategy.SuppressTests(testClass);
            _logger.LogDebug("Tests suppressed successfully for {TestClass}", testClass.FullName);
            return suppressionResult;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "An error occurred while tests suppression for {TestClass}", testClass.FullName);
            throw;
        }
    }
    
    private ITestSuppressionStrategy ResolveSuppressionStrategy(ITestEngine testEngine)
    {
        if (_suppressionStrategies.TryGetValue(testEngine.GetType(), out var suppressionStrategy))
        {
            _logger.LogDebug("Suppression strategy found for {TestEngineType}", testEngine.GetType());
            return suppressionStrategy;
        }
        
        _logger.LogError("No suppression strategy found for {TestEngineType}", testEngine.GetType());
        throw new InvalidOperationException($"No suppression strategy found for the given test engine and tests query combination: {testEngine.GetType()}");
    }
}
