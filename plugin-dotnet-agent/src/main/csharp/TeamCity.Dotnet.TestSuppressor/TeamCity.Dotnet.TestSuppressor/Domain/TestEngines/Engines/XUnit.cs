namespace TeamCity.Dotnet.TestSuppressor.Domain.TestEngines.Engines;

internal class XUnit : ITestEngine
{
    public string Name => "xUnit";

    public IEnumerable<string> AssemblyNames => new[]
    {
        "xunit.core",
    };

    public IReadOnlyList<string> TestClassAttributes { get; } = Array.Empty<string>();

    public IReadOnlyList<string> TestMethodAttributes { get; } = new []
    {
        "Xunit.TheoryAttribute",
        "Xunit.FactAttribute"
    };
}