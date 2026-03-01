package fr.aetheris.api.endpoint;

@FunctionalInterface
public interface EndpointHandler {

    EndpointResponse handle(EndpointRequest request);
}
