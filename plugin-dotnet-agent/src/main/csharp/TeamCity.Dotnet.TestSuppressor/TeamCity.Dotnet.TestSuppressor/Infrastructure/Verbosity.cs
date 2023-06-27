using System.ComponentModel;
using System.Globalization;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure;

[AttributeUsage(AttributeTargets.Field, AllowMultiple = true)]
public class AliasAttribute : Attribute
{
    public string Alias { get; }

    public AliasAttribute(string alias)
    {
        Alias = alias;
    }
}

[TypeConverter(typeof(VerbosityTypeConverter))]
public enum Verbosity
{
    [Alias("q"), Alias("quiet")]
    Quiet,      // no logs

    [Alias("min"), Alias("minimal")]
    Minimal,    // errors only

    [Alias("n"), Alias("normal")]
    Normal,     // default (information logs)

    [Alias("d"), Alias("detailed")]
    Detailed,   // debug

    [Alias("diag"), Alias("diagnostic")]
    Diagnostic  // with trace
}

public class VerbosityTypeConverter : TypeConverter
{
    public override bool CanConvertFrom(ITypeDescriptorContext? context, Type sourceType) =>
        sourceType == typeof(string) || base.CanConvertFrom(context, sourceType);

    public override object? ConvertFrom(ITypeDescriptorContext? context, CultureInfo? culture, object value)
    {
        if (value is string input)
        {
            foreach (Verbosity verbosity in Enum.GetValues(typeof(Verbosity)))
            {
                foreach (var alias in GetAliases(verbosity))
                {
                    if (alias.Equals(input, StringComparison.OrdinalIgnoreCase))
                    {
                        return verbosity;
                    }
                }
            }

            return Enum.TryParse<Verbosity>(input, true, out var result) ? result : Verbosity.Normal;
        }

        return base.ConvertFrom(context, culture, value);
    }

    private IEnumerable<string> GetAliases(Verbosity verbosity)
    {
        var fieldInfo = verbosity.GetType().GetField(verbosity.ToString());
        var aliasAttributes = (AliasAttribute[])Attribute.GetCustomAttributes(fieldInfo!, typeof(AliasAttribute));
        return aliasAttributes.Select(attribute => attribute.Alias).ToList();
    }
}