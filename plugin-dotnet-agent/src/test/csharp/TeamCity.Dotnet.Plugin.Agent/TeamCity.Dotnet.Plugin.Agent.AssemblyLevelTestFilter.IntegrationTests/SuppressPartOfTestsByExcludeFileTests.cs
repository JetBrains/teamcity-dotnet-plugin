// /*
//  * Copyright 2000-2023 JetBrains s.r.o.
//  *
//  * Licensed under the Apache License, Version 2.0 (the "License");
//  * you may not use this file except in compliance with the License.
//  * You may obtain a copy of the License at
//  *
//  * http://www.apache.org/licenses/LICENSE-2.0
//  *
//  * Unless required by applicable law or agreed to in writing, software
//  * distributed under the License is distributed on an "AS IS" BASIS,
//  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  * See the License for the specific language governing permissions and
//  * limitations under the License.
//  */

using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Extensions;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Fixtures;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Fixtures.TestProjects;
using Xunit.Abstractions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests;

public class SuppressPartOfTestsByExcludeFileTests : IClassFixture<DotnetTestContainerFixture>
{
    private readonly DotnetTestContainerFixture _fixture;

    public SuppressPartOfTestsByExcludeFileTests(DotnetTestContainerFixture fixture, ITestOutputHelper output)
    {
        _fixture = fixture;
        _fixture.Init(output);
    }

    [Theory]
    [InlineData(typeof(XUnitTestProject), DotnetVersion.v8_0_Preview)]
    [InlineData(typeof(XUnitTestProject), DotnetVersion.v7_0)]
    [InlineData(typeof(XUnitTestProject), DotnetVersion.v6_0)]
    [InlineData(typeof(NUnitTestProject), DotnetVersion.v8_0_Preview)]
    [InlineData(typeof(NUnitTestProject), DotnetVersion.v7_0)]
    [InlineData(typeof(NUnitTestProject), DotnetVersion.v6_0)]
    [InlineData(typeof(MsTestTestProject), DotnetVersion.v8_0_Preview)]
    [InlineData(typeof(MsTestTestProject), DotnetVersion.v7_0)]
    [InlineData(typeof(MsTestTestProject), DotnetVersion.v6_0)]
    public async Task Run(Type testProjectGeneratorType, DotnetVersion dotnetVersion)
    {
        // arrange
        await _fixture.ReinitContainerWith(dotnetVersion);
        
        const string projectName = "MyTestProject";
        var testClass0 = new TestClassDescription("TestClass0", "Test0", "Test1", "Test2");
        var testClass1 = new TestClassDescription("TestClass1", "Test0", "Test1", "Test2", "Test3");
        var testClass2 = new TestClassDescription("TestClass2", "Test0", "Test1");
        var allTestClasses = new[] { testClass0, testClass1, testClass2 };
        var allTestsNames = allTestClasses.GetFullTestMethodsNames(projectName);
        var testNamesToInclude = testClass1.GetFullTestMethodsNames(projectName);
        var testClassesToExclude = new[] { testClass0, testClass2 };
        var testNamesToExclude = testClassesToExclude.GetFullTestMethodsNames(projectName);

        var (testQueriesFilePath, targetPath) = await _fixture.CreateTestProject(
            testProjectGeneratorType, 
            dotnetVersion,
            projectName,
            withoutDebugSymbols: false,
            DotnetTestContainerFixture.TargetType.Assembly,
            allTestClasses,
            testClassesToExclude
        );

        // act
        var (beforeTestOutput, beforeTestNamesExecuted) = await _fixture.RunTests(targetPath);
        await _fixture.RunFilterApp($"suppress -t {targetPath} -l {testQueriesFilePath} -v detailed");
        var (afterTestOutput, afterTestNamesExecuted) = await _fixture.RunTests(targetPath);

        // assert
        Assert.Equal(9, beforeTestNamesExecuted.Count);
        Assert.True(allTestsNames.ContainsSameElements(beforeTestNamesExecuted));
        Assert.Contains($"Passed!  - Failed:     0, Passed:     9, Skipped:     0, Total:     9", beforeTestOutput.Stdout);
        Assert.Equal(testNamesToInclude.Count, afterTestNamesExecuted.Count);
        Assert.True(testNamesToInclude.ContainsSameElements(afterTestNamesExecuted));
        Assert.False(testNamesToExclude.Intersect(afterTestNamesExecuted).Any());
        Assert.Contains($"Passed!  - Failed:     0, Passed:     4, Skipped:     0, Total:     4", afterTestOutput.Stdout);
    }
}