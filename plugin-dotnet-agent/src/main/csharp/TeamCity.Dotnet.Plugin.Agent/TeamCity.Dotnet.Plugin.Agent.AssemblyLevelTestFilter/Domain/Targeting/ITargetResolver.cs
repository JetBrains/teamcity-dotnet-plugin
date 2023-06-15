using System.IO.Abstractions;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting;

internal interface ITargetResolver
{
    IEnumerable<IFileInfo> Resolve(string target);
}