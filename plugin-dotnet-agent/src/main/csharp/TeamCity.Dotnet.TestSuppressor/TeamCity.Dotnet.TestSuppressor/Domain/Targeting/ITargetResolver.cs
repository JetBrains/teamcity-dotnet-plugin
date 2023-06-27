using System.IO.Abstractions;

namespace TeamCity.Dotnet.TestSuppressor.Domain.Targeting;

internal interface ITargetResolver
{
    IEnumerable<IFileInfo> Resolve(string target);
}