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
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

internal class TestSelectorsLoader : ITestSelectorsLoader
{
    private readonly ITestSelectorParser _testSelectorParser;
    private readonly IFileSystem _fileSystem;
    private readonly ILogger<TestSelectorsLoader> _logger;

    public TestSelectorsLoader(
        ITestSelectorParser testSelectorParser,
        IFileSystem fileSystem,
        ILogger<TestSelectorsLoader> logger)
    {
        _testSelectorParser = testSelectorParser;
        _fileSystem = fileSystem;
        _logger = logger;
    }

    public async Task<IReadOnlyDictionary<string, ITestSelector>> LoadTestSelectorsFromAsync(string filePath)
    {
        _logger.LogInformation("Loading test selectors from file: {TestClassesFilePath}", filePath);
        
        var testSelectorsFile = TryToGetSelectorsFile(filePath);
        if (testSelectorsFile == null)
        {
            _logger.LogWarning("Test selectors file is not available: {Target}", filePath);
            return new Dictionary<string, ITestSelector>();
        }

        return await LoadSelectors(testSelectorsFile);
    }

    private async Task<IReadOnlyDictionary<string, ITestSelector>> LoadSelectors(FileInfo testSelectorsFile)
    {
        var registry = new Dictionary<string, ITestSelector>();

        await foreach (var (line, lineNumber) in _fileSystem.ReadLinesAsync(testSelectorsFile.FullName))
        {
            _logger.LogDebug("Reading {LineNumber} line from test query file: {TestQueryFileLine}", lineNumber, line);
            
            var testQueryFileLine = line.Trim();
            if (string.IsNullOrWhiteSpace(testQueryFileLine))
            {
                _logger.LogDebug("Skip empty line number {LineNumber} from test query file", lineNumber);
                continue;
            }

            if (testQueryFileLine.TrimStart().StartsWith("#"))
            {
                _logger.LogDebug("Skip the control line from test query file: {TestQueryFileLine}", testQueryFileLine);
                continue;
            }

            if (_testSelectorParser.TryParseTestQuery(testQueryFileLine, out var testSelector))
            {
                registry[testSelector!.Query] = testSelector;
                _logger.LogDebug("Loaded test selector: {TestQuery}", testSelector.Query);
            }
            else
            {
                _logger.LogWarning("Failed to parse test selector: {TestQuery}", testQueryFileLine);
            }
        }

        _logger.LogInformation("Loaded {RegistryCount} test selectors from the file {File}", registry.Count, testSelectorsFile.FullName);

        return registry;
    }
    
    private FileInfo? TryToGetSelectorsFile(string filePath)
    {
        if (!_fileSystem.FileExists(filePath))
        {
            _logger.LogWarning("Test selectors file not found: {Target}", filePath);
            return null;
        }

        var (testSelectorsFile, exception) = _fileSystem.GetFileInfo(filePath);
        if (exception != null)
        {
            _logger.Log(LogLevel.Warning, exception,"Can't access to test selectors file: {Target}", filePath);
            return null;
        }

        return testSelectorsFile;
    }
}