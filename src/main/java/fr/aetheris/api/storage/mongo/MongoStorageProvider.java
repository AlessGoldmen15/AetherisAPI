package fr.aetheris.api.storage.mongo;

import fr.aetheris.api.storage.KeyValueStore;
import fr.aetheris.api.storage.StorageProvider;

public final class MongoStorageProvider implements StorageProvider {

    private final MongoAdapter adapter;

    public MongoStorageProvider(MongoAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public KeyValueStore open(String namespace) {
        return new MongoKeyValueStore(adapter, namespace);
    }

    @Override
    public void close() {
    }
}
