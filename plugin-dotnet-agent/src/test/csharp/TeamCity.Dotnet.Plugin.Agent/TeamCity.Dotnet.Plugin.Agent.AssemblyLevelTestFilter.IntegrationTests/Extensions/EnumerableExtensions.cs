namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Extensions;

public static class EnumerableExtensions
{
    public static bool ContainsSameElements<T>(this IEnumerable<T> source, IEnumerable<T> target) =>
        source.OrderDescending().SequenceEqual(target.OrderDescending());
}