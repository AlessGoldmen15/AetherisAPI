package fr.aetheris.api.storage.memory;

import fr.aetheris.api.storage.KeyValueStore;
import fr.aetheris.api.storage.StorageProvider;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryStorageProvider implements StorageProvider {

    private final Map<String, InMemoryKeyValueStore> stores = new ConcurrentHashMap<>();

    @Override
    public KeyValueStore open(String namespace) {
        return stores.computeIfAbsent(namespace, ignored -> new InMemoryKeyValueStore());
    }

    @Override
    public void close() {
        stores.clear();
    }
}
