namespace TeamCity.Dotnet.TestSuppressor.IntegrationTests.Fixtures.TestProjects;

internal record TestProjectSettings(
    IReadOnlySet<DotnetVersion> TargetFrameworks,
    string DirectoryPath,
    string ProjectName,
    bool WithSolution
);