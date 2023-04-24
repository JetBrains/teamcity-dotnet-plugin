namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting;

using System.Collections.Generic;
using System.IO;

internal interface ITargetResolvingStrategy
{
    TargetType TargetType { get; }
    
    IAsyncEnumerable<FileInfo> FindAssembliesAsync(string target);
}