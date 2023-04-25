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
            throw new ArgumentException("Test selector query cannot be empty or null.", nameof(testSelectorQuery));
        }

        var isParametrized = testSelectorQuery.Contains("(");
        var querySegments = testSelectorQuery.Split('.');
        IList<string> namespaces = querySegments.Take(querySegments.Length - 1).ToList();
        string className;

        if (inclusionMode)
        {
            if (testSelectors.TryGetValue(testSelectorQuery, out var existingSelector))
            {
                return (false, existingSelector);
            }

            throw new InvalidOperationException($"Test selector with query '{testSelectorQuery}' not found in testSelectors.");
        }
        else
        {
            if (isParametrized)
            {
                var paramStartIndex = testSelectorQuery.IndexOf("(");
                var paramEndIndex = testSelectorQuery.IndexOf(")");
                className = querySegments.Last().Substring(0, paramStartIndex - querySegments.Length + 1);
                var parametersString = testSelectorQuery.Substring(paramStartIndex + 1, paramEndIndex - paramStartIndex - 1);
                var parameters = parametersString.Split(',').Select(p => p.Trim()).ToList();
                var selector = new ParamTestClassSelector(namespaces, className, parameters);
                return (true, selector);
            }
            else
            {
                className = querySegments.Last();
                var selector = new TestClassSelector(namespaces, className);
                return (true, selector);
            }
        }
    }
}
