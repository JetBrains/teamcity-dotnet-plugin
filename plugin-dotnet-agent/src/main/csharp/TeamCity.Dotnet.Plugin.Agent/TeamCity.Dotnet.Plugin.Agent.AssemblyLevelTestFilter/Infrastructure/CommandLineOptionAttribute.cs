namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure;

[AttributeUsage(AttributeTargets.Property)]
public class CommandLineOptionAttribute : Attribute
{
    public IEnumerable<string> Options { get; }

    public CommandLineOptionAttribute(params string[] options)
    {
        Options = options;
    }
}
