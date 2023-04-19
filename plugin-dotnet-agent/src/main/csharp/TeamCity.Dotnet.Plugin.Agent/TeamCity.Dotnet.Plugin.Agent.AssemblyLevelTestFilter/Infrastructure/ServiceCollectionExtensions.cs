using System.Reflection;
using Microsoft.Extensions.DependencyInjection;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure;

public static class ServiceCollectionExtensions
{
    public static IServiceCollection AddSingletonByInterface<TInterface>(this IServiceCollection services) =>
        AddServicesByInterface<TInterface>(services, ServiceLifetime.Singleton);

    private static IServiceCollection AddServicesByInterface<TInterface>(this IServiceCollection services, ServiceLifetime lifetime)
    {
        var interfaceType = typeof(TInterface);

        Assembly.GetCallingAssembly()
            .GetTypes()
            .Where(type => !type.IsInterface && !type.IsAbstract && interfaceType.IsAssignableFrom(type))
            .Select(type => new
            {
                ServiceType = type.GetInterfaces().FirstOrDefault(i => i == interfaceType),
                ImplementationType = type
            })
            .Where(x => x.ServiceType != null)
            .ToList()
            .ForEach(x => services.Add(new ServiceDescriptor(x.ServiceType, x.ImplementationType, lifetime)));

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