using System.IO.Abstractions;
using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

internal class TestSelectorsLoader : ITestSelectorsLoader
{
    private readonly ITestSelectorParser _testSelectorParser;
    private readonly IFileSystem _fileSystem;
    private readonly IFileReader _fileReader;
    private readonly ILogger<TestSelectorsLoader> _logger;

    public TestSelectorsLoader(
        ITestSelectorParser testSelectorParser,
        IFileSystem fileSystem,
        IFileReader fileReader,
        ILogger<TestSelectorsLoader> logger)
    {
        _testSelectorParser = testSelectorParser;
        _fileSystem = fileSystem;
        _fileReader = fileReader;
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

    private async Task<IReadOnlyDictionary<string, ITestSelector>> LoadSelectors(IFileSystemInfo testSelectorsFile)
    {
        var registry = new Dictionary<string, ITestSelector>();

        await foreach (var (line, lineNumber) in _fileReader.ReadLinesAsync(testSelectorsFile.FullName))
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
    
    private IFileInfo? TryToGetSelectorsFile(string filePath)
    {
        if (!_fileSystem.File.Exists(filePath))
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