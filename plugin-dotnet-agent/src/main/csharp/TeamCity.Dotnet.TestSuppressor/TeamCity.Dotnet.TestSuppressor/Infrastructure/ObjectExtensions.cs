namespace TeamCity.Dotnet.TestSuppressor.Infrastructure;

internal static class ObjectExtensions
{
    public static T? GetPropertyValue<T>(this object? obj, string fieldName) where T : class
    {
        if (obj == null)
        {
            throw new ArgumentNullException(nameof(obj), $"Parameter {nameof(obj)} is null");
        }
        return (obj.GetType().GetProperty(fieldName)?.GetValue(obj) ?? default(T)) as T;
    }
}