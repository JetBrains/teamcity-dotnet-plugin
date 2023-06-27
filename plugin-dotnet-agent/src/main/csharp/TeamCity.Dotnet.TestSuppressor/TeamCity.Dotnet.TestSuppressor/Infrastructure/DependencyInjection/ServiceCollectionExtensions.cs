using System.Reflection;
using Microsoft.Extensions.DependencyInjection;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.DependencyInjection;

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
}