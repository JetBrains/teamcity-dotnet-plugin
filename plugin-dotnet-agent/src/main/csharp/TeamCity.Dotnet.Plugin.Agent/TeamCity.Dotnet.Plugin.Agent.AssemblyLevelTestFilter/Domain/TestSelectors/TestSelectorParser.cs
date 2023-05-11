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

using System.Text.RegularExpressions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

/// <summary>
/// Parses test query string into <see cref="ITestSelector"/> instance
/// Possible test query formats:
/// NamespacesA.NamespaceB.NamespaceC.ClassName
/// NamespacesA.NamespaceB.NamespaceC.ClassName(param1,param2)
/// </summary>
internal class TestSelectorParser : ITestSelectorParser
{
    private static readonly Regex Regex = new (@"^(?<namespaces>([^.]+\.)*)(?<class>[^(]+)(\((?<params>[^)]+)\))?$");
    
    public ITestSelector? ParseTestQuery(string testQueryLine)
    {
        if (string.IsNullOrWhiteSpace(testQueryLine))
            return null;

        var match = Regex.Match(testQueryLine);

        if (!match.Success)
        {
            throw new ArgumentException($"Invalid test query format: {testQueryLine}");
        }

        var namespaces = match.Groups["namespaces"].Value.Split('.').Where(s => !string.IsNullOrEmpty(s)).ToList();
        var className = match.Groups["class"].Value;

        return match.Groups["params"].Success
            ? new ParamTestClassSelector(namespaces, className, ParseParameters(match.Groups["params"].Value))
            : new TestClassSelector(namespaces, className);
    }

    private static List<string> ParseParameters(string paramString) =>
        paramString.Split(',').Select(p => p.Trim()).ToList();
}
