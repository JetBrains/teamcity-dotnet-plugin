namespace TeamCity.Dotnet.TestSuppressor.Domain.Patching;

internal record AssemblyMutationResult(int AffectedTypes, int AffectedMethods)
{
    public static AssemblyMutationResult? Empty => new(0, 0);
}