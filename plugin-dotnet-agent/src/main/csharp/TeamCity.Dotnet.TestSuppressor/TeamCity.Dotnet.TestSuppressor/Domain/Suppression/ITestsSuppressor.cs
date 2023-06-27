using TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Suppression;

internal interface ITestsSuppressor
{
    TestSuppressionResult SuppressTests(IDotnetType type, TestSuppressionParameters parameters);
}