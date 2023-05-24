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

using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;

internal class TestSuppressionDecider : ITestSuppressionDecider
{
    public (bool shouldBeSuppressed, ITestSelector testSelector) Decide(string testSelectorQuery, bool inclusionMode, IReadOnlyDictionary<string, ITestSelector> testSelectors)
    {
        if (string.IsNullOrWhiteSpace(testSelectorQuery))
        {
            throw new ArgumentException("Test selector query cannot be empty or null", nameof(testSelectorQuery));
        }

        // works only for test class selectors without parameters
        var (namespaces, className) = Parse(testSelectorQuery);

        return testSelectors.TryGetValue(testSelectorQuery, out var existingSelector)
            ? (shouldBeSuppressed: !inclusionMode, testSelector: existingSelector)
            : (shouldBeSuppressed: inclusionMode, testSelector: new TestClassSelector(namespaces, className));
    }

    private static (IList<string>, string) Parse(string testSelectorQuery)
    {
        var parenthesisIndex = testSelectorQuery.IndexOf('(');
        var querySegments = parenthesisIndex != -1
            ? testSelectorQuery[..parenthesisIndex].Split('.')
            : testSelectorQuery.Split('.');
        IList<string> namespaces = querySegments.Take(querySegments.Length - 1).ToList();
        return (namespaces, querySegments.Last());
    }
}
