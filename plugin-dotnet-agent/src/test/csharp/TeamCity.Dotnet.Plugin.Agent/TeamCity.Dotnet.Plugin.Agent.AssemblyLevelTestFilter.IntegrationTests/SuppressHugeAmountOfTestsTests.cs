using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Extensions;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Fixtures;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Fixtures.TestProjects;
using Xunit.Abstractions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests;

public class SuppressHugeAmountOfTestsTests : IClassFixture<DotnetTestContainerFixture>
{
    private readonly DotnetTestContainerFixture _fixture;

    public SuppressHugeAmountOfTestsTests(DotnetTestContainerFixture fixture, ITestOutputHelper output)
    {
        _fixture = fixture;
        _fixture.Init(output);
    }

    [Theory]
    [InlineData(DotnetVersion.v8_0_Preview)]
    [InlineData(DotnetVersion.v7_0)]
    [InlineData(DotnetVersion.v6_0)]
    public async Task Run(DotnetVersion dotnetVersion)
    {
        // arrange
        await _fixture.ReinitContainerWith(dotnetVersion);
        
        const string projectName = "MyTestProject";
        var allTestClasses = GenerateTestClasses(10000, 2);
        var allTestsNames = allTestClasses.GetFullTestMethodsNames(projectName);
        var (testClassesToInclude, testClassesToExclude) = SplitRandomly(allTestClasses,5000);
        var testNamesToInclude = testClassesToInclude.GetFullTestMethodsNames(projectName);
        var testNamesToExclude = testClassesToExclude.GetFullTestMethodsNames(projectName);

        var (testQueriesFilePath, targetPath) = await _fixture.CreateTestProject(
            typeof(XUnitTestProject),   // it doesn't matter which project type we use here
            dotnetVersion,
            projectName,
            DotnetTestContainerFixture.TargetType.Assembly, // it doesn't matter which target type we use here
            allTestClasses,
            testClassesToInclude
        );

        // act
        var (_, beforeTestNamesExecuted) = await _fixture.RunTests(targetPath);
        await _fixture.RunFilterApp($"suppress -t {targetPath} -l {testQueriesFilePath} -i -v detailed"); // `-i` stands for "inclusion mode"
        var (_, afterTestNamesExecuted) = await _fixture.RunTests(targetPath);

        // assert
        Assert.Equal(20000, beforeTestNamesExecuted.Count);
        Assert.True(allTestsNames.ContainsSameElements(beforeTestNamesExecuted));
        Assert.Equal(testNamesToInclude.Count, afterTestNamesExecuted.Count);
        Assert.True(testNamesToInclude.ContainsSameElements(afterTestNamesExecuted));
        Assert.False(testNamesToExclude.Intersect(afterTestNamesExecuted).Any());
    }
    
    private static (TestClassDescription[], TestClassDescription[]) SplitRandomly(TestClassDescription[] input, int takeCount)
    {
        var rnd = new Random();
        var indexes = Enumerable.Range(0, input.Length).OrderBy(_ => rnd.Next()).ToList();
    
        var taken = indexes.Take(takeCount).Select(i => input[i]).ToArray();
        var remaining = indexes.Skip(takeCount).Select(i => input[i]).ToArray();

        return (taken, remaining);
    }

    private static TestClassDescription[] GenerateTestClasses(int classCount, int testCountPerClass)
    {
        var result = new TestClassDescription[classCount];

        for (var i = 0; i < classCount; i++)
        {
            var className = $"TestClass{i}";

            var testMethods = new string[testCountPerClass];
            for (var j = 0; j < testCountPerClass; j++)
            {
                testMethods[j] = $"Test{j}";
            }

            result[i] = new TestClassDescription(className, testMethods);
        }

        return result;
    }
}

