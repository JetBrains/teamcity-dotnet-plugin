namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;

[AttributeUsage(AttributeTargets.Property)]
public class CommandDescriptionAttribute : Attribute
{
    public string Description { get; set; }

    public CommandDescriptionAttribute(string description)
    {
        Description = description;
    }
}