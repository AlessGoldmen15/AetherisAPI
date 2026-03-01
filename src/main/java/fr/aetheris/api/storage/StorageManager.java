package fr.aetheris.api.storage;

public interface StorageManager extends AutoCloseable {

    void registerProvider(StorageBackendType backendType, StorageProvider provider);

    KeyValueStore open(StorageBackendType backendType, String namespace);

    KeyValueStore open(String namespace);

    void setDefaultBackend(StorageBackendType backendType);

    @Override
    void close();
}
