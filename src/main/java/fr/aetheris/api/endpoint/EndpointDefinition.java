package fr.aetheris.api.endpoint;

public record EndpointDefinition(
        HttpMethod method,
        String path,
        String requiredPermission,
        EndpointHandler handler
) {
}
