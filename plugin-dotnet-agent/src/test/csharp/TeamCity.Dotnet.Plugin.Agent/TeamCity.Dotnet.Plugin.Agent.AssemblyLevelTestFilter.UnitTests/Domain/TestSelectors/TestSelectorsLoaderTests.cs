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

using System.IO.Abstractions;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;
using Moq;
using Microsoft.Extensions.Logging;

#pragma warning disable CS1998

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.Domain.TestSelectors;

public class TestSelectorsLoaderTests
{
    private readonly Mock<ITestSelectorParser> _selectorParserMock = new();
    private readonly Mock<IFileSystem> _fileSystemMock;
    private readonly Mock<IFileReader> _fileReaderMock;
    private readonly ITestSelectorsLoader _loader;

    public TestSelectorsLoaderTests()
    {
        _fileSystemMock = new Mock<IFileSystem>();
        _fileReaderMock = new Mock<IFileReader>();
        var loggerMock = new Mock<ILogger<TestSelectorsLoader>>();
        _loader = new TestSelectorsLoader(_selectorParserMock.Object, _fileSystemMock.Object, _fileReaderMock.Object, loggerMock.Object);
    }

    [Fact]
    public async Task LoadTestSelectorsFromAsync_Should_Return_Empty_If_File_Not_Exists()
    {
        _fileSystemMock.Setup(fs => fs.File.Exists(It.IsAny<string>())).Returns(false);
        var result = await _loader.LoadTestSelectorsFromAsync("path/to/file");
        Assert.Empty(result);
    }

    [Fact]
    public async Task LoadTestSelectorsFromAsync_ShouldSkipControlLines()
    {
        // arrange
        const string filePath = "/path/to/file";
        var fileInfoMock = new Mock<IFileInfo>();
        fileInfoMock.Setup(fi => fi.FullName).Returns(filePath);
        _fileSystemMock.Setup(fs => fs.File.Exists(It.IsAny<string>())).Returns(true);
        _fileSystemMock.Setup(m => m.File.Exists(It.IsAny<string>())).Returns(true);
        _fileSystemMock.Setup(m => m.FileInfo.New(It.IsAny<string>())).Returns(fileInfoMock.Object);
        _fileReaderMock.Setup(m => m.ReadLinesAsync(It.IsAny<string>()))
            .Returns(ToAsyncEnumerable(new List<(string, int)> { ("#this is a comment", 1) }));
        
        // act
        var result = await _loader.LoadTestSelectorsFromAsync("path/to/file");
        
        // assert
        Assert.Empty(result);
    }

    [Fact]
    public async Task LoadTestSelectorsFromAsync_Should_Skip_Empty_Lines()
    {
        // arrange
        var fileInfoMock = new Mock<IFileInfo>();
        _fileSystemMock.Setup(fs => fs.File.Exists(It.IsAny<string>())).Returns(true);
        _fileSystemMock.Setup(m => m.File.Exists(It.IsAny<string>())).Returns(true);
        _fileSystemMock.Setup(m => m.FileInfo.New(It.IsAny<string>())).Returns(fileInfoMock.Object);
        _fileReaderMock.Setup(m => m.ReadLinesAsync(It.IsAny<string>()))
            .Returns(ToAsyncEnumerable(new List<(string, int)> { ("", 1) }));
        
        // act
        var result = await _loader.LoadTestSelectorsFromAsync("path/to/file");
        
        // assert
        Assert.Empty(result);
    }
    
    [Fact]
    public async Task LoadTestSelectorsFromAsync_Should_Return_Correct_Selector_When_Parser_Returns_True()
    {
        // arrange
        var testSelector = new Mock<ITestSelector>();
        testSelector.Setup(ts => ts.Query).Returns("valid_selector");

        var fileInfoMock = new Mock<IFileInfo>();
        _fileSystemMock.Setup(fs => fs.File.Exists(It.IsAny<string>())).Returns(true);
        _fileSystemMock.Setup(m => m.File.Exists(It.IsAny<string>())).Returns(true);
        _fileSystemMock.Setup(m => m.FileInfo.New(It.IsAny<string>())).Returns(fileInfoMock.Object);
        _fileReaderMock.Setup(fs => fs.ReadLinesAsync(It.IsAny<string>()))
            .Returns(ToAsyncEnumerable(new List<(string, int)> { ("valid_selector", 1) }));
    
        _selectorParserMock.Setup(sp => sp.TryParseTestQuery(It.IsAny<string>(), out It.Ref<ITestSelector>.IsAny!))
            .Returns(true)
            .Callback(new TryParseTestQueryCallback((string s, out ITestSelector ts) => ts = testSelector.Object));
    
        // act
        var result = await _loader.LoadTestSelectorsFromAsync("path/to/file");
        
        // assert
        Assert.Single(result);
        Assert.True(result.ContainsKey("valid_selector"));
        Assert.Equal(testSelector.Object, result["valid_selector"]);
    }

    private delegate void TryParseTestQueryCallback(string s, out ITestSelector ts);

    private static async IAsyncEnumerable<T> ToAsyncEnumerable<T>(IEnumerable<T> enumerable)
    {
        foreach (var item in enumerable)
        {
            yield return item;
        }
    }
}