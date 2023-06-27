using Mono.Cecil;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.DotnetAssembly;

internal class DotnetType : IDotnetType
{
    private readonly TypeDefinition _typeDefinition;
    
    public DotnetType(TypeDefinition typeDefinition)
    {
        _typeDefinition = typeDefinition;
    }

    public string FullName => _typeDefinition.FullName;
    
    public IEnumerable<IDotnetCustomAttribute> CustomAttributes =>
        _typeDefinition.CustomAttributes.Select(a => new DotnetCustomAttribute(a));
    
    public IEnumerable<IDotnetMethod> Methods =>
        _typeDefinition.Methods.Select(m => new DotnetMethod(m));
    
    public void RemoveCustomAttribute(IDotnetCustomAttribute customAttribute)
    {
        var wrapper = (DotnetCustomAttribute) customAttribute;
        _typeDefinition.CustomAttributes.Remove(wrapper.CustomAttribute);
    }
}