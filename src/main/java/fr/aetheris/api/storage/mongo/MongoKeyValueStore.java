package fr.aetheris.api.storage.mongo;

import fr.aetheris.api.storage.KeyValueStore;
import java.util.Optional;
import java.util.Set;

public final class MongoKeyValueStore implements KeyValueStore {

    private final MongoAdapter adapter;
    private final String namespace;

    public MongoKeyValueStore(MongoAdapter adapter, String namespace) {
        this.adapter = adapter;
        this.namespace = namespace;
    }

    @Override
    public void put(String key, String value) {
        adapter.put(namespace, key, value);
    }

    @Override
    public Optional<String> get(String key) {
        return adapter.get(namespace, key);
    }

    @Override
    public void delete(String key) {
        adapter.delete(namespace, key);
    }

    @Override
    public Set<String> keys() {
        return adapter.keys(namespace);
    }

    @Override
    public void close() {
    }
}
