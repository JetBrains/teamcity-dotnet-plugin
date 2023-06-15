using System.IO.Abstractions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting;

internal interface ITargetResolvingStrategy
{
    TargetType TargetType { get; }
    
    IEnumerable<(IFileSystemInfo, TargetType)> Resolve(string target);
}