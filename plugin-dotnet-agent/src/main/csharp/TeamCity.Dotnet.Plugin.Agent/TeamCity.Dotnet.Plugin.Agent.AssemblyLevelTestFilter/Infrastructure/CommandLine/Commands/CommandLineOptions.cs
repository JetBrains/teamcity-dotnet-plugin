using System.Reflection;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;

public static class CommandLineOptions<T>
{
    public static IDictionary<string, string> Mappings
    {
        get
        {
            var mappings = new Dictionary<string, string>();
            var className = typeof(T).Name;
            var properties = typeof(T).GetProperties();

            foreach (var property in properties)
            {
                var attribute = property.GetCustomAttribute<CommandLineOptionAttribute>();
                if (attribute == null)
                {
                    continue;
                }
                    
                var configPath = $"{className}:{property.Name}";
                foreach (var option in attribute.Options)
                {
                    mappings.Add(option, configPath);
                }
            }

            return mappings;
        }
    }
}