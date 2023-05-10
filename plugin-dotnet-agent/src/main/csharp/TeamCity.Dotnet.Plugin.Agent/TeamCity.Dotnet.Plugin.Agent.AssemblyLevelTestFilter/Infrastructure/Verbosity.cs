/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using System.ComponentModel;
using System.Globalization;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure;

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