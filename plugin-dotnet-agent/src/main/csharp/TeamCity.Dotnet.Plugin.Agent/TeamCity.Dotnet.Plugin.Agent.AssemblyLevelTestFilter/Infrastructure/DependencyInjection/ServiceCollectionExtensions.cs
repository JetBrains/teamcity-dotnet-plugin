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

using System.Reflection;
using Microsoft.Extensions.DependencyInjection;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.DependencyInjection;

public static class ServiceCollectionExtensions
{
    public static IServiceCollection AddSingletonByInterface<TInterface>(this IServiceCollection services) =>
        AddServicesByInterface<TInterface>(services, ServiceLifetime.Singleton);

    public static IServiceCollection AddSingletonByImplementationType<TInterface>(this IServiceCollection services) =>
        AddServicesByImplementationType<TInterface>(services, ServiceLifetime.Singleton);

    private static IServiceCollection AddServicesByInterface<TInterface>(this IServiceCollection services, ServiceLifetime lifetime)
    {
        var interfaceType = typeof(TInterface);

        Assembly.GetCallingAssembly()
            .GetTypes()
            .Where(type => type is { IsInterface: false, IsAbstract: false } && interfaceType.IsAssignableFrom(type))
            .Select(type => new
            {
                ServiceType = type.GetInterfaces().FirstOrDefault(i => i == interfaceType),
                ImplementationType = type
            })
            .Where(x => x.ServiceType != null)
            .ToList()
            .ForEach(x => services.Add(new ServiceDescriptor(x.ServiceType!, x.ImplementationType, lifetime)));

        return services;
    }

    private static IServiceCollection AddServicesByImplementationType<TImplementation>(this IServiceCollection services, ServiceLifetime lifetime)
    {
        var targetType = typeof(TImplementation);

        Assembly.GetCallingAssembly()
            .GetTypes()
            .Where(type => !type.IsInterface && !type.IsAbstract && targetType.IsAssignableFrom(type))
            .ToList()
            .ForEach(implementationType =>
            {
                var serviceType = implementationType.GetInterfaces().FirstOrDefault(i => i != targetType);
                services.Add(serviceType != null
                    ? new ServiceDescriptor(serviceType, implementationType, lifetime)
                    : new ServiceDescriptor(implementationType, implementationType, lifetime));
            });

        return services;
    }
    
    public static void DisplayRegisteredServices(this IServiceCollection services)
    {
        Console.WriteLine("Registered services:");
        foreach (var service in services)
        {
            Console.WriteLine($"ServiceType: {service.ServiceType.FullName}, ImplementationType: {service.ImplementationType?.FullName ?? "Factory/Instance"}");
        }
    }
}