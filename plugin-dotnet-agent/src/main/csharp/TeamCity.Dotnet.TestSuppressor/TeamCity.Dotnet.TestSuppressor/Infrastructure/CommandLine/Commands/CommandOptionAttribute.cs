namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Commands;

[AttributeUsage(AttributeTargets.Property)]
public class CommandOptionAttribute : Attribute
{ 
    public IEnumerable<string> Options { get; }

    public bool RequiresValue { get; }

    public CommandOptionAttribute(bool requiresValue, params string[] options)
    {
        RequiresValue = requiresValue;
        Options = options;
    }
}