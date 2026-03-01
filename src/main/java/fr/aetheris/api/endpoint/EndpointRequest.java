package fr.aetheris.api.endpoint;

import java.util.Map;

public record EndpointRequest(
        String subjectId,
        Map<String, String> pathParameters,
        Map<String, String> queryParameters,
        String body
) {
}
