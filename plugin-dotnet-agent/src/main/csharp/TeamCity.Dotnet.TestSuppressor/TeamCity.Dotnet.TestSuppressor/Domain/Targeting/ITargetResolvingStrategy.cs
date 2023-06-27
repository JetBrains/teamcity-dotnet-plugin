using System.IO.Abstractions;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Targeting;

internal interface ITargetResolvingStrategy
{
    TargetType TargetType { get; }
    
    IEnumerable<(IFileSystemInfo, TargetType)> Resolve(string target);
}