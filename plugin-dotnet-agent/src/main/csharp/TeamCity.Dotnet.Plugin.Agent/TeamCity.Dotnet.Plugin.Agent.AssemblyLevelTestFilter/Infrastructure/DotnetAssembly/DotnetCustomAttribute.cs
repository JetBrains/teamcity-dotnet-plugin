using Mono.Cecil;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DotnetAssembly;

internal class DotnetCustomAttribute : IDotnetCustomAttribute
{
    public DotnetCustomAttribute(CustomAttribute customAttribute)
    {
        CustomAttribute = customAttribute;
    }

    public CustomAttribute CustomAttribute { get; }

    public string FullName => CustomAttribute.AttributeType.FullName;
}