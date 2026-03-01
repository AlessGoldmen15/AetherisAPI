package fr.aetheris.api.endpoint;

import fr.aetheris.api.security.PermissionService;
import java.util.Objects;

public final class EndpointDispatcher {

    private final EndpointRegistry endpointRegistry;
    private final PermissionService permissionService;

    public EndpointDispatcher(EndpointRegistry endpointRegistry, PermissionService permissionService) {
        this.endpointRegistry = Objects.requireNonNull(endpointRegistry, "endpointRegistry must not be null");
        this.permissionService = Objects.requireNonNull(permissionService, "permissionService must not be null");
    }

    public EndpointResponse dispatch(HttpMethod method, String path, EndpointRequest request) {
        return endpointRegistry.resolve(method, path)
                .map(endpoint -> execute(endpoint, request))
                .orElseGet(() -> EndpointResponse.notFound("Endpoint not found"));
    }

    private EndpointResponse execute(EndpointDefinition endpoint, EndpointRequest request) {
        final String requiredPermission = endpoint.requiredPermission();
        if (requiredPermission != null && !requiredPermission.isBlank()) {
            if (request.subjectId() == null || !permissionService.hasPermission(request.subjectId(), requiredPermission)) {
                return EndpointResponse.forbidden("Missing permission: " + requiredPermission);
            }
        }
        return endpoint.handler().handle(request);
    }
}
