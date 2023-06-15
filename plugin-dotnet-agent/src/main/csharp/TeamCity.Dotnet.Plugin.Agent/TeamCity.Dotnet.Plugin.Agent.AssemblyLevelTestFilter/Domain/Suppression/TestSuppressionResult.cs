namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Suppression;

public record struct TestSuppressionResult(int SuppressedTests, int SuppressedClasses);