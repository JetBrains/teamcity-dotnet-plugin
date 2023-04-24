namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines.XUnit;

internal class XUnit : ITestEngine
{
    public string Name => "xUnit";

    public IList<string> TestClassAttributes { get; } = new List<string>
    {
        "Xunit.FactAttribute"
    };

    public IList<string> TestMethodAttributes { get; } = new List<string>
    {
        "Xunit.TheoryAttribute",
        "Xunit.FactAttribute"
    };
}