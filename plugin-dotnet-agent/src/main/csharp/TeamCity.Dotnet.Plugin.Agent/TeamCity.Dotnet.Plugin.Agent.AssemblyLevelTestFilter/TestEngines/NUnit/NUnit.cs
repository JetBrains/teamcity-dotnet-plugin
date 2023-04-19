using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.TestEngines;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.TestEngines.NUnit;

internal class NUnit : ITestEngine
{
    public string Name => "NUnit";

    public IList<string> TestClassAttributes { get; } = new List<string>
    {
        "NUnit.Framework.TestFixtureAttribute",
        "NUnit.Framework.TestFixtureSourceAttribute"
    };

    public IList<string> TestMethodAttributes { get; } = new List<string>
    {
        "NUnit.Framework.TestAttribute",
        "NUnit.Framework.TestCaseAttribute",
        "NUnit.Framework.TestCaseSourceAttribute"
    };
}