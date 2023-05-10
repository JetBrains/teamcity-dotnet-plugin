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

using Mono.Cecil;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

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

    private IEnumerable<MethodDefinition> GetTestMethods(TypeDefinition type) =>
        type.Methods
            .Where(method => method.CustomAttributes
                .Select(a => a.AttributeType.FullName)
                .Any(TestEngine.TestMethodAttributes.Contains)
            );
    
    protected TestSuppressionResult RemoveAllTestAttributes(TypeDefinition type)
    {
        var (suppressedTests, suppressedClasses) = RemoveTestAttributesFromClass(type);
        suppressedTests += RemoveTestAttributesFromMethods(type);
        return new TestSuppressionResult(suppressedTests, suppressedClasses);
    }

    public abstract TestSuppressionResult SuppressTestsBySelector(TypeDefinition type, TTestSelector testSelector);

    public TestSuppressionResult SuppressTests(TypeDefinition type, ITestSelector testSelector) =>
        SuppressTestsBySelector(type, (TTestSelector) testSelector);

    private int RemoveTestAttributesFromMethods(TypeDefinition testClass)
    {
        var suppressedTests = 0;
        foreach (var method in GetTestMethods(testClass))
        {
            foreach (var testAttribute in GetMethodsTestAttributes(method))
            {
                method.CustomAttributes.Remove(testAttribute);
            }
            suppressedTests++;
        }

        return suppressedTests;
    }

    private (int, int) RemoveTestAttributesFromClass(TypeDefinition testClass)
    {
        var (suppressedTests, suppressedClasses) = (0, 0);
        foreach (var testAttribute in GetTypeTestAttributes(testClass))
        {
            suppressedTests += GetTestMethods(testClass).Count();
            suppressedClasses++;
            testClass.CustomAttributes.Remove(testAttribute);
        }
        return (suppressedTests, suppressedClasses);
    }

    private List<CustomAttribute> GetMethodsTestAttributes(MethodDefinition method)
    {
        return method.CustomAttributes
            .Where(a => TestEngine.TestMethodAttributes.Contains(a.AttributeType.FullName))
            .ToList();
    }

    private List<CustomAttribute> GetTypeTestAttributes(TypeDefinition testClass)
    {
        return testClass.CustomAttributes
            .Where(a => TestEngine.TestClassAttributes.Contains(a.AttributeType.FullName))
            .ToList();
    }
}