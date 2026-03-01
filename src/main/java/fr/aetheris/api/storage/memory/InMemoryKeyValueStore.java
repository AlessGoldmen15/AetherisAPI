package fr.aetheris.api.storage.memory;

import fr.aetheris.api.storage.KeyValueStore;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryKeyValueStore implements KeyValueStore {

    private final Map<String, String> data = new ConcurrentHashMap<>();

    @Override
    public void put(String key, String value) {
        data.put(key, value);
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(data.get(key));
    }

    @Override
    public void delete(String key) {
        data.remove(key);
    }

    @Override
    public Set<String> keys() {
        return Set.copyOf(data.keySet());
    }

    @Override
    public void close() {
        data.clear();
    }
}
