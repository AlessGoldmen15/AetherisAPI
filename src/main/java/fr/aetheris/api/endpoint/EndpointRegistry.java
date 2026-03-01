package fr.aetheris.api.endpoint;

import java.util.Collection;
import java.util.Optional;

public interface EndpointRegistry {

    void register(EndpointDefinition endpoint);

    Optional<EndpointDefinition> resolve(HttpMethod method, String path);

    Collection<EndpointDefinition> all();

    void clear();
}
