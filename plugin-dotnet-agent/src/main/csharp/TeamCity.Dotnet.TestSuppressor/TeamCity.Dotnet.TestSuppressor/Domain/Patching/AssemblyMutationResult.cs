namespace TeamCity.Dotnet.TestSuppressor.Domain.Patching;

internal record AssemblyMutationResult(int AffectedTypes, int AffectedMethods)
{
    public static AssemblyMutationResult Empty => new(0, 0);

    public bool IsEmpty => this is { AffectedTypes: 0, AffectedMethods: 0 };
}