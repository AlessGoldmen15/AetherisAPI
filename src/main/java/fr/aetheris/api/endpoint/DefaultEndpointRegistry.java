package fr.aetheris.api.endpoint;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultEndpointRegistry implements EndpointRegistry {

    private final Map<String, EndpointDefinition> endpoints = new ConcurrentHashMap<>();

    @Override
    public void register(EndpointDefinition endpoint) {
        validate(endpoint);
        endpoints.put(key(endpoint.method(), endpoint.path()), endpoint);
    }

    @Override
    public Optional<EndpointDefinition> resolve(HttpMethod method, String path) {
        if (method == null || path == null || path.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(endpoints.get(key(method, path)));
    }

    @Override
    public Collection<EndpointDefinition> all() {
        return Collections.unmodifiableCollection(endpoints.values());
    }

    @Override
    public void clear() {
        endpoints.clear();
    }

    private static String key(HttpMethod method, String path) {
        return method.name() + ":" + normalize(path);
    }

    private static String normalize(String path) {
        return path.trim().replaceAll("/{2,}", "/").toLowerCase();
    }

    private static void validate(EndpointDefinition endpoint) {
        if (endpoint == null) {
            throw new IllegalArgumentException("endpoint must not be null");
        }
        if (endpoint.method() == null) {
            throw new IllegalArgumentException("endpoint method must not be null");
        }
        if (endpoint.path() == null || endpoint.path().isBlank()) {
            throw new IllegalArgumentException("endpoint path must not be blank");
        }
        if (endpoint.handler() == null) {
            throw new IllegalArgumentException("endpoint handler must not be null");
        }
    }
}
