package fr.aetheris.api.storage.mongo;

import java.util.Optional;
import java.util.Set;

public interface MongoAdapter {

    void put(String namespace, String key, String value);

    Optional<String> get(String namespace, String key);

    void delete(String namespace, String key);

    Set<String> keys(String namespace);
}
