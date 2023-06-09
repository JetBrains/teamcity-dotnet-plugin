/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

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
    
    public TestSuppressionResult SuppressTests(IDotnetType testClass, TestSuppressionParameters parameters)
    {
        try
        {
            var suppressionStrategy = ResolveSuppressingStrategy(parameters.TestEngine, parameters.TestSelector);
            var suppressionResult = suppressionStrategy.SuppressTests(testClass, parameters.TestSelector);
            _logger.LogInformation("Tests suppressed successfully for {TestClass}", testClass.FullName);
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
