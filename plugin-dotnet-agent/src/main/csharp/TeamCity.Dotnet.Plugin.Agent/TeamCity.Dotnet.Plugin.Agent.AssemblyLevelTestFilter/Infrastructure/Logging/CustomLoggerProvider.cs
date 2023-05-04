/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.Logging;

internal class CustomLoggerProvider<TCommand> : ILoggerProvider
    where TCommand : Command
{
    private readonly IOptions<TCommand> _options;
    private readonly IEnumerable<ILoggerConfigurator> _loggerConfigurators;

    public CustomLoggerProvider(
        IOptions<TCommand> options, 
        IEnumerable<ILoggerConfigurator> loggerConfigurators)
    {
        _options = options;
        _loggerConfigurators = loggerConfigurators;
    }

    public ILogger CreateLogger(string categoryName)
    {
        var command = _options.Value;
        var loggerConfigurator = ChooseConfigurator(command.Verbosity);
        var loggerFactory = LoggerFactory.Create(builder => loggerConfigurator.Configure(builder));
        return loggerFactory.CreateLogger(categoryName);
    }

    private ILoggerConfigurator ChooseConfigurator(Verbosity verbosity = Verbosity.Normal) =>
        _loggerConfigurators.FirstOrDefault(x => x.Verbosity == verbosity)
        ?? _loggerConfigurators.First(x => x.Verbosity == Verbosity.Normal);

    public void Dispose() {}
}