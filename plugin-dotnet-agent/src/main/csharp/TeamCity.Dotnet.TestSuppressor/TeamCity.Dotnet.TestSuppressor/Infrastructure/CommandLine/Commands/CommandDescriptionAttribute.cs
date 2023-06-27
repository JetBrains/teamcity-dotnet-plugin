namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Commands;

[AttributeUsage(AttributeTargets.Property)]
public class CommandDescriptionAttribute : Attribute
{
    public string Description { get; set; }

    public CommandDescriptionAttribute(string description)
    {
        Description = description;
    }
}