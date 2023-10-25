using Microsoft.Build.Logging;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.MsBuild;

internal static class MsBuildExtensions
{
    public static bool HasItemOfType(this Record record, string itemType) =>
        record.Args.GetPropertyValue<string>("ItemType")?.Equals(itemType, StringComparison.InvariantCulture) ?? false;
    
    public static IEnumerable<string> GetItemsSpecs(this Record record) =>
        record.Args.GetPropertyValue<object[]>("Items")
            ?.Select(i => i.GetPropertyValue<string>("ItemSpec"))
            .Where(s => s != null)
            .Select(s => s!)
        ?? Array.Empty<string>();
}