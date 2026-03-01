package fr.aetheris.api.storage;

import java.util.Optional;
import java.util.Set;

public interface KeyValueStore extends AutoCloseable {

    void put(String key, String value);

    Optional<String> get(String key);

    void delete(String key);

    Set<String> keys();

    @Override
    void close();
}
