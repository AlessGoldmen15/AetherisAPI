package fr.aetheris.api.storage;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultStorageManager implements StorageManager {

    private final Map<StorageBackendType, StorageProvider> providers = new ConcurrentHashMap<>();
    private volatile StorageBackendType defaultBackend = StorageBackendType.MEMORY;

    @Override
    public void registerProvider(StorageBackendType backendType, StorageProvider provider) {
        Objects.requireNonNull(backendType, "backendType must not be null");
        Objects.requireNonNull(provider, "provider must not be null");
        providers.put(backendType, provider);
    }

    @Override
    public KeyValueStore open(StorageBackendType backendType, String namespace) {
        Objects.requireNonNull(backendType, "backendType must not be null");
        final StorageProvider provider = providers.get(backendType);
        if (provider == null) {
            throw new StorageException("No storage provider registered for backend " + backendType);
        }
        return provider.open(namespace);
    }

    @Override
    public KeyValueStore open(String namespace) {
        return open(defaultBackend, namespace);
    }

    @Override
    public void setDefaultBackend(StorageBackendType backendType) {
        Objects.requireNonNull(backendType, "backendType must not be null");
        if (!providers.containsKey(backendType)) {
            throw new StorageException("Cannot set default backend to " + backendType + " because no provider is registered.");
        }
        defaultBackend = backendType;
    }

    @Override
    public void close() {
        for (StorageProvider provider : providers.values()) {
            provider.close();
        }
        providers.clear();
    }
}
