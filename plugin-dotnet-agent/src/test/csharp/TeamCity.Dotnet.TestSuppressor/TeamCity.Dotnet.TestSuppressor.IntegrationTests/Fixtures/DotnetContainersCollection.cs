namespace TeamCity.Dotnet.TestSuppressor.IntegrationTests.Fixtures;

[CollectionDefinition(".NET containers")]
public class DotnetContainersCollection : ICollectionFixture<DotnetTestContainerFixture> {}