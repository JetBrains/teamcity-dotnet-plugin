using Mono.Cecil;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;

internal class DotnetMethod : IDotnetMethod
{
    private readonly MethodDefinition _methodDefinition;

    public DotnetMethod(MethodDefinition methodDefinition)
    {
        _methodDefinition = methodDefinition;
    }

    public IEnumerable<IDotnetCustomAttribute> CustomAttributes =>
        _methodDefinition.CustomAttributes.Select(attr => new DotnetCustomAttribute(attr));

    public void RemoveCustomAttribute(IDotnetCustomAttribute attribute)
    {
        var wrapper = (DotnetCustomAttribute) attribute;
        _methodDefinition.CustomAttributes.Remove(wrapper.CustomAttribute);
    }
}