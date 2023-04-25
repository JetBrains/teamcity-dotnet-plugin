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
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;

internal class TestSelectorsFactory : ITestSelectorsFactory
{
    private readonly ITestSelectorParser _testSelectorParser;
    private readonly ILogger<TestSelectorsFactory> _logger;

    public TestSelectorsFactory(ITestSelectorParser testSelectorParser, ILogger<TestSelectorsFactory> logger)
    {
        
        _testSelectorParser = testSelectorParser;
        _logger = logger;
    }

    public Task<IReadOnlyDictionary<string, ITestSelector>> LoadFromAsync(string filePath)
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
        return Task.FromResult(LoadSelectors(filePath));
    }

    private IReadOnlyDictionary<string, ITestSelector> LoadSelectors(string filePath)
    {
        var registry = new Dictionary<string, ITestSelector>();
        
        if (!File.Exists(filePath))
        {
            _logger.LogError("File {FilePath} not found", filePath);
            throw new FileNotFoundException($"File '{filePath}' not found");
        }

        foreach (var line in File.ReadAllLines(filePath))
        {
            if (string.IsNullOrWhiteSpace(line) || line.TrimStart().StartsWith("#"))
            {
                continue;
            }

            var testSelector = _testSelectorParser.ParseTestQuery(line.Trim());

            if (testSelector != null)
            {
                registry[testSelector.Query] = testSelector;
            }
        }

        _logger.LogInformation("Loaded {RegistryCount} test criteria from file", registry.Count);

        return registry;
    }
}