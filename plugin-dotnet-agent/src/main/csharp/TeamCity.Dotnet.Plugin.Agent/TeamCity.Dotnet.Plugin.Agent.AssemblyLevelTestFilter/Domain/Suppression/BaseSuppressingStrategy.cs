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

using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;

internal abstract class BaseSuppressingStrategy<TTestEngine, TTestSelector> : ITestSuppressingStrategy
    where TTestEngine : ITestEngine
    where TTestSelector : ITestSelector
{
    protected BaseSuppressingStrategy(TTestEngine testEngine)
    {
        TestEngine = testEngine;
    }

    private TTestEngine TestEngine { get; }

    private IEnumerable<IDotnetMethod> GetTestMethods(IDotnetType type) =>
        type.Methods
            .Where(method => method.CustomAttributes
                .Select(a => a.FullName)
                .Any(TestEngine.TestMethodAttributes.Contains)
            );
    
    protected TestSuppressionResult RemoveAllTestAttributes(IDotnetType type)
    {
        var (suppressedTests, suppressedClasses) = RemoveTestAttributesFromClass(type);
        suppressedTests += RemoveTestAttributesFromMethods(type);
        return new TestSuppressionResult(suppressedTests, suppressedClasses);
    }

    public abstract TestSuppressionResult SuppressTestsBySelector(IDotnetType type, TTestSelector testSelector);

    public TestSuppressionResult SuppressTests(IDotnetType type, ITestSelector testSelector) =>
        SuppressTestsBySelector(type, (TTestSelector) testSelector);

    private int RemoveTestAttributesFromMethods(IDotnetType testClass)
    {
        var suppressedTests = 0;
        foreach (var method in GetTestMethods(testClass))
        {
            foreach (var testAttribute in GetMethodsTestAttributes(method))
            {
                method.RemoveCustomAttribute(testAttribute);
            }
            suppressedTests++;
        }

        return suppressedTests;
    }

    private (int, int) RemoveTestAttributesFromClass(IDotnetType testClass)
    {
        var (suppressedTests, suppressedClasses) = (0, 0);
        foreach (var testAttribute in GetTypeTestAttributes(testClass))
        {
            suppressedTests += GetTestMethods(testClass).Count();
            suppressedClasses++;
            testClass.RemoveCustomAttribute(testAttribute);
        }
        return (suppressedTests, suppressedClasses);
    }

    private List<IDotnetCustomAttribute> GetMethodsTestAttributes(IDotnetMethod method)
    {
        return method.CustomAttributes
            .Where(a => TestEngine.TestMethodAttributes.Contains(a.FullName))
            .ToList();
    }

    private List<IDotnetCustomAttribute> GetTypeTestAttributes(IDotnetType testClass)
    {
        return testClass.CustomAttributes
            .Where(a => TestEngine.TestClassAttributes.Contains(a.FullName))
            .ToList();
    }
}