using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestSelectors;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;

internal record TestSuppressionCriteria(bool ShouldBeSuppressed, ITestsSelector TestsSelector);
