namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Parsing;

internal interface IConfigurationParsingResult
{
    IDictionary<string, string> Mappings { get; }
    
    IList<string> UnknownParameters { get; }
}