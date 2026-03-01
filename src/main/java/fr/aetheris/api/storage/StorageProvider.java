package fr.aetheris.api.storage;

public interface StorageProvider extends AutoCloseable {

    KeyValueStore open(String namespace);

    @Override
    void close();
}
