namespace TeamCity.Dotnet.TestSuppressor.Domain.Suppression;

public record struct TestSuppressionResult(int SuppressedTests, int SuppressedClasses);