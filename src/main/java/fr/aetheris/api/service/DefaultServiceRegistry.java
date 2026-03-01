package fr.aetheris.api.service;

import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultServiceRegistry implements ServiceRegistry {

    private final Map<Class<? extends AetherisService>, AetherisService> services = new ConcurrentHashMap<>();

    @Override
    public <T extends AetherisService> void register(Class<T> serviceClass, T service) {
        if (serviceClass == null || service == null) {
            throw new IllegalArgumentException("serviceClass and service must not be null");
        }
        services.put(serviceClass, service);
    }

    @Override
    public <T extends AetherisService> Optional<T> find(Class<T> serviceClass) {
        final AetherisService service = services.get(serviceClass);
        if (service == null) {
            return Optional.empty();
        }
        return Optional.of(serviceClass.cast(service));
    }

    @Override
    public <T extends AetherisService> T require(Class<T> serviceClass) {
        return find(serviceClass)
                .orElseThrow(() -> new NoSuchElementException("No service registered for " + serviceClass.getName()));
    }

    @Override
    public boolean unregister(Class<? extends AetherisService> serviceClass) {
        return services.remove(serviceClass) != null;
    }

    @Override
    public Set<Class<? extends AetherisService>> registeredTypes() {
        return Collections.unmodifiableSet(services.keySet());
    }

    @Override
    public void clear() {
        services.clear();
    }
}
