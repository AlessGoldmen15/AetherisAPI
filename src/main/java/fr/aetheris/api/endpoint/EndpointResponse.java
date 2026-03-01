package fr.aetheris.api.endpoint;

import java.util.Map;

public record EndpointResponse(
        int statusCode,
        String body,
        Map<String, String> headers
) {

    public static EndpointResponse ok(String body) {
        return new EndpointResponse(200, body, Map.of());
    }

    public static EndpointResponse forbidden(String body) {
        return new EndpointResponse(403, body, Map.of());
    }

    public static EndpointResponse notFound(String body) {
        return new EndpointResponse(404, body, Map.of());
    }
}
