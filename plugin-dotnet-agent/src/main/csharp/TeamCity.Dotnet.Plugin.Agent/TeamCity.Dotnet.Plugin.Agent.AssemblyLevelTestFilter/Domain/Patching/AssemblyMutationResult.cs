namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Patching;

internal record AssemblyMutationResult(int AffectedTypes, int AffectedMethods)
{
    public static AssemblyMutationResult Empty => new(0, 0);
}