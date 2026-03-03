package fr.aetheris.api.domain.gui;

import fr.aetheris.api.endpoint.EndpointDefinition;
import fr.aetheris.api.endpoint.EndpointRegistry;
import fr.aetheris.api.endpoint.EndpointRequest;
import fr.aetheris.api.endpoint.EndpointResponse;
import fr.aetheris.api.endpoint.HttpMethod;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public final class GuiEndpoints {

    private GuiEndpoints() {
    }

    public static void registerCrud(GuiService guiService, EndpointRegistry endpointRegistry) {
        registerCrud(guiService, endpointRegistry, "gui.manage", "gui.use");
    }

    public static void registerCrud(
            GuiService guiService,
            EndpointRegistry endpointRegistry,
            String managePermission,
            String usePermission
    ) {
        endpointRegistry.register(new EndpointDefinition(HttpMethod.POST, "/guis/create", managePermission, request -> saveGui(guiService, request)));
        endpointRegistry.register(new EndpointDefinition(HttpMethod.PUT, "/guis/update", managePermission, request -> saveGui(guiService, request)));
        endpointRegistry.register(new EndpointDefinition(HttpMethod.GET, "/guis/get", usePermission, request -> getGui(guiService, request)));
        endpointRegistry.register(new EndpointDefinition(HttpMethod.GET, "/guis/list", usePermission, request -> listGuis(guiService)));
        endpointRegistry.register(new EndpointDefinition(HttpMethod.DELETE, "/guis/delete", managePermission, request -> deleteGui(guiService, request)));
        endpointRegistry.register(new EndpointDefinition(HttpMethod.POST, "/guis/open", usePermission, request -> openGui(guiService, request)));
    }

    private static EndpointResponse saveGui(GuiService guiService, EndpointRequest request) {
        try {
            GuiDefinition definition = parseDefinition(request);
            guiService.save(definition);
            return EndpointResponse.ok(toJson(definition));
        } catch (IllegalArgumentException exception) {
            return badRequest(exception.getMessage());
        }
    }

    private static EndpointResponse getGui(GuiService guiService, EndpointRequest request) {
        String guiId = request.queryParameters().get("id");
        if (guiId == null || guiId.isBlank()) {
            return badRequest("Missing query parameter: id");
        }
        return guiService.byId(guiId)
                .map(definition -> EndpointResponse.ok(toJson(definition)))
                .orElseGet(() -> EndpointResponse.notFound("GUI not found"));
    }

    private static EndpointResponse listGuis(GuiService guiService) {
        List<GuiDefinition> definitions = guiService.all();
        StringBuilder builder = new StringBuilder();
        builder.append('{').append("\"count\":").append(definitions.size()).append(",\"guis\":[");
        for (int index = 0; index < definitions.size(); index++) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append(toJson(definitions.get(index)));
        }
        builder.append("]}");
        return EndpointResponse.ok(builder.toString());
    }

    private static EndpointResponse deleteGui(GuiService guiService, EndpointRequest request) {
        String guiId = request.queryParameters().get("id");
        if (guiId == null || guiId.isBlank()) {
            return badRequest("Missing query parameter: id");
        }
        boolean deleted = guiService.delete(guiId);
        if (!deleted) {
            return EndpointResponse.notFound("GUI not found");
        }
        return EndpointResponse.ok("{\"deleted\":true,\"id\":\"" + escape(guiId) + "\"}");
    }

    private static EndpointResponse openGui(GuiService guiService, EndpointRequest request) {
        Map<String, String> input = collectInput(request);
        String guiId = input.get("id");
        if (guiId == null || guiId.isBlank()) {
            return badRequest("Missing field: id");
        }

        String playerId = Optional.ofNullable(input.get("playerId")).orElse(request.subjectId());
        if (playerId == null || playerId.isBlank()) {
            return badRequest("Missing field: playerId");
        }

        Map<String, String> context = new HashMap<>();
        for (Map.Entry<String, String> entry : input.entrySet()) {
            if (entry.getKey().startsWith("context.")) {
                String key = entry.getKey().substring("context.".length());
                if (!key.isBlank()) {
                    context.put(key, entry.getValue());
                }
            }
        }

        GuiRenderResult result = guiService.open(guiId, new GuiOpenContext(playerId, context));
        if (!result.success()) {
            return EndpointResponse.notFound("GUI not found");
        }
        return EndpointResponse.ok("{\"success\":true,\"message\":\"" + escape(result.message()) + "\",\"gui\":" + toJson(result.gui()) + "}");
    }

    private static GuiDefinition parseDefinition(EndpointRequest request) {
        Map<String, String> data = collectInput(request);

        String id = required(data, "id");
        String title = required(data, "title");
        GuiType type = parseGuiType(data.getOrDefault("type", "CHEST"));
        int rows = parseInt(data.getOrDefault("rows", "0"), "rows");
        int size = parseInt(data.getOrDefault("size", "0"), "size");
        String requiredPermission = data.get("permission");

        Map<String, String> metadata = parseMetadata(data.get("metadata"));
        List<GuiButton> buttons = parseButtons(data.get("buttons"));

        return new GuiDefinition(id, title, type, rows, size, requiredPermission, buttons, metadata);
    }

    private static Map<String, String> collectInput(EndpointRequest request) {
        Map<String, String> input = new HashMap<>();
        input.putAll(parseBodyAsProperties(request.body()));
        input.putAll(request.queryParameters());
        return input;
    }

    private static Map<String, String> parseBodyAsProperties(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.isBlank()) {
            return map;
        }

        Properties properties = new Properties();
        try {
            properties.load(new StringReader(body));
        } catch (IOException exception) {
            return map;
        }

        for (String key : properties.stringPropertyNames()) {
            map.put(key, properties.getProperty(key));
        }
        return map;
    }

    private static List<GuiButton> parseButtons(String serializedButtons) {
        List<GuiButton> buttons = new ArrayList<>();
        if (serializedButtons == null || serializedButtons.isBlank()) {
            return buttons;
        }

        for (String rawButton : serializedButtons.split(";")) {
            String[] parts = rawButton.trim().split(",");
            if (parts.length < 4) {
                continue;
            }

            int slot = parseInt(parts[0].trim(), "button slot");
            String label = parts[1].trim();
            GuiActionType actionType = parseActionType(parts[2].trim());
            String payload = parts[3].trim();
            String icon = parts.length >= 5 ? parts[4].trim() : "";
            boolean closeOnClick = parts.length >= 6 && Boolean.parseBoolean(parts[5].trim());

            buttons.add(new GuiButton(slot, label, icon, actionType, payload, closeOnClick));
        }

        return buttons;
    }

    private static Map<String, String> parseMetadata(String serializedMetadata) {
        Map<String, String> metadata = new HashMap<>();
        if (serializedMetadata == null || serializedMetadata.isBlank()) {
            return metadata;
        }

        for (String token : serializedMetadata.split(";")) {
            String[] pair = token.split(":", 2);
            if (pair.length != 2) {
                continue;
            }
            String key = pair[0].trim();
            String value = pair[1].trim();
            if (!key.isBlank()) {
                metadata.put(key, value);
            }
        }
        return metadata;
    }

    private static EndpointResponse badRequest(String message) {
        return new EndpointResponse(400, "{\"error\":\"" + escape(message) + "\"}", Map.of());
    }

    private static String toJson(GuiDefinition definition) {
        StringBuilder builder = new StringBuilder();
        builder.append('{')
                .append("\"id\":\"").append(escape(definition.id())).append("\",")
                .append("\"title\":\"").append(escape(definition.title())).append("\",")
                .append("\"type\":\"").append(definition.type().name()).append("\",")
                .append("\"rows\":").append(definition.rows()).append(',')
                .append("\"size\":").append(definition.size()).append(',')
                .append("\"permission\":").append(definition.requiredPermission() == null ? "null" : "\"" + escape(definition.requiredPermission()) + "\"")
                .append(',')
                .append("\"buttons\":").append(definition.buttons().size())
                .append('}');
        return builder.toString();
    }

    private static String required(Map<String, String> data, String key) {
        String value = data.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing field: " + key);
        }
        return value;
    }

    private static GuiType parseGuiType(String rawType) {
        try {
            return GuiType.valueOf(rawType.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown GUI type: " + rawType, exception);
        }
    }

    private static GuiActionType parseActionType(String rawType) {
        try {
            return GuiActionType.valueOf(rawType.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown GUI action type: " + rawType, exception);
        }
    }

    private static int parseInt(String value, String field) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid int for " + field + ": " + value, exception);
        }
    }

    private static String escape(String raw) {
        return raw
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
