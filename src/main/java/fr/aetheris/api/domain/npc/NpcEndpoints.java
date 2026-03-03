package fr.aetheris.api.domain.npc;

import fr.aetheris.api.endpoint.EndpointDefinition;
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

public final class NpcEndpoints {

    private NpcEndpoints() {
    }

    public static void registerCrud(NpcService npcService, fr.aetheris.api.endpoint.EndpointRegistry endpointRegistry) {
        registerCrud(npcService, endpointRegistry, "npc.manage", "npc.use");
    }

    public static void registerCrud(
            NpcService npcService,
            fr.aetheris.api.endpoint.EndpointRegistry endpointRegistry,
            String managePermission,
            String usePermission
    ) {
        endpointRegistry.register(new EndpointDefinition(
                HttpMethod.POST,
                "/npcs/create",
                managePermission,
                request -> saveNpc(npcService, request)
        ));

        endpointRegistry.register(new EndpointDefinition(
                HttpMethod.PUT,
                "/npcs/update",
                managePermission,
                request -> saveNpc(npcService, request)
        ));

        endpointRegistry.register(new EndpointDefinition(
                HttpMethod.GET,
                "/npcs/get",
                usePermission,
                request -> getNpc(npcService, request)
        ));

        endpointRegistry.register(new EndpointDefinition(
                HttpMethod.GET,
                "/npcs/list",
                usePermission,
                request -> listNpcs(npcService, request)
        ));

        endpointRegistry.register(new EndpointDefinition(
                HttpMethod.DELETE,
                "/npcs/delete",
                managePermission,
                request -> deleteNpc(npcService, request)
        ));

        endpointRegistry.register(new EndpointDefinition(
                HttpMethod.POST,
                "/npcs/interact",
                usePermission,
                request -> interactNpc(npcService, request)
        ));
    }

    private static EndpointResponse saveNpc(NpcService npcService, EndpointRequest request) {
        try {
            NpcDefinition definition = parseDefinition(request);
            npcService.save(definition);
            return EndpointResponse.ok(toJson(definition));
        } catch (IllegalArgumentException exception) {
            return badRequest(exception.getMessage());
        }
    }

    private static EndpointResponse getNpc(NpcService npcService, EndpointRequest request) {
        String npcId = request.queryParameters().get("id");
        if (npcId == null || npcId.isBlank()) {
            return badRequest("Missing query parameter: id");
        }
        return npcService.byId(npcId)
                .map(definition -> EndpointResponse.ok(toJson(definition)))
                .orElseGet(() -> EndpointResponse.notFound("NPC not found"));
    }

    private static EndpointResponse listNpcs(NpcService npcService, EndpointRequest request) {
        String rawType = request.queryParameters().get("type");
        List<NpcDefinition> definitions;
        if (rawType == null || rawType.isBlank()) {
            definitions = npcService.all();
        } else {
            try {
                definitions = npcService.byType(NpcType.valueOf(rawType.toUpperCase()));
            } catch (IllegalArgumentException exception) {
                return badRequest("Unknown NPC type: " + rawType);
            }
        }

        StringBuilder builder = new StringBuilder();
        builder.append('{').append("\"count\":").append(definitions.size()).append(",\"npcs\":[");
        for (int index = 0; index < definitions.size(); index++) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append(toJson(definitions.get(index)));
        }
        builder.append("]}");
        return EndpointResponse.ok(builder.toString());
    }

    private static EndpointResponse deleteNpc(NpcService npcService, EndpointRequest request) {
        String npcId = request.queryParameters().get("id");
        if (npcId == null || npcId.isBlank()) {
            return badRequest("Missing query parameter: id");
        }
        boolean deleted = npcService.delete(npcId);
        if (!deleted) {
            return EndpointResponse.notFound("NPC not found");
        }
        return EndpointResponse.ok("{\"deleted\":true,\"id\":\"" + escape(npcId) + "\"}");
    }

    private static EndpointResponse interactNpc(NpcService npcService, EndpointRequest request) {
        Map<String, String> data = collectInput(request);
        String npcId = data.get("id");
        if (npcId == null || npcId.isBlank()) {
            return badRequest("Missing field: id");
        }

        String playerId = Optional.ofNullable(data.get("playerId")).orElse(request.subjectId());
        if (playerId == null || playerId.isBlank()) {
            return badRequest("Missing field: playerId");
        }

        NpcInteractionType interactionType = parseInteractionType(data.get("interactionType"));

        Map<String, String> context = new HashMap<>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (entry.getKey().startsWith("context.")) {
                String key = entry.getKey().substring("context.".length());
                if (!key.isBlank()) {
                    context.put(key, entry.getValue());
                }
            }
        }

        NpcInteractionResult result = npcService.interact(
                npcId,
                new NpcInteractionContext(playerId, interactionType, context)
        );

        String json = "{\"success\":" + result.success() +
                ",\"message\":\"" + escape(result.message()) + "\"" +
                ",\"actions\":" + result.actions().size() + "}";
        return EndpointResponse.ok(json);
    }

    private static NpcDefinition parseDefinition(EndpointRequest request) {
        Map<String, String> data = collectInput(request);

        String id = required(data, "id");
        String displayName = required(data, "name");
        NpcType type = parseNpcType(required(data, "type"));

        NpcLocation location = new NpcLocation(
                required(data, "world"),
                parseDouble(required(data, "x"), "x"),
                parseDouble(required(data, "y"), "y"),
                parseDouble(required(data, "z"), "z"),
                parseFloat(data.getOrDefault("yaw", "0"), "yaw"),
                parseFloat(data.getOrDefault("pitch", "0"), "pitch")
        );

        Map<String, String> attributes = parseAttributes(data.get("attributes"));
        List<NpcAction> actions = parseActions(data);

        return new NpcDefinition(id, displayName, type, location, attributes, actions);
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

    private static List<NpcAction> parseActions(Map<String, String> data) {
        List<NpcAction> actions = new ArrayList<>();

        String teleport = data.get("teleport");
        if (teleport != null && !teleport.isBlank()) {
            actions.add(new TeleportAction(parseLocation(teleport)));
        }

        String stats = data.get("stats");
        if (stats != null && !stats.isBlank()) {
            for (String token : stats.split(";")) {
                String[] parts = token.trim().split(",");
                if (parts.length != 3) {
                    continue;
                }
                actions.add(new StatAction(
                        parts[0].trim(),
                        parseDouble(parts[2].trim(), "stats value"),
                        StatOperation.valueOf(parts[1].trim().toUpperCase())
                ));
            }
        }

        String loot = data.get("loot");
        if (loot != null && !loot.isBlank()) {
            List<NpcLootEntry> entries = new ArrayList<>();
            for (String token : loot.split(";")) {
                String[] parts = token.trim().split(",");
                if (parts.length != 4) {
                    continue;
                }
                entries.add(new NpcLootEntry(
                        parts[0].trim(),
                        parseInt(parts[1].trim(), "loot min"),
                        parseInt(parts[2].trim(), "loot max"),
                        parseDouble(parts[3].trim(), "loot chance")
                ));
            }
            if (!entries.isEmpty()) {
                actions.add(new LootAction(entries));
            }
        }

        String merchant = data.get("merchant");
        if (merchant != null && !merchant.isBlank()) {
            List<NpcMerchantOffer> offers = new ArrayList<>();
            for (String token : merchant.split(";")) {
                String[] parts = token.trim().split(",");
                if (parts.length != 4) {
                    continue;
                }
                offers.add(new NpcMerchantOffer(
                        parts[0].trim(),
                        parseInt(parts[1].trim(), "offer amount"),
                        parseDouble(parts[2].trim(), "offer price"),
                        parts[3].trim()
                ));
            }
            if (!offers.isEmpty()) {
                actions.add(new MerchantAction(offers));
            }
        }

        String dialogue = data.get("dialogue");
        String dialogueId = data.getOrDefault("dialogueId", "default");
        if (dialogue != null && !dialogue.isBlank()) {
            List<String> lines = new ArrayList<>();
            for (String line : dialogue.split("\\|")) {
                if (!line.isBlank()) {
                    lines.add(line.trim());
                }
            }
            actions.add(new DialogueAction(dialogueId, lines));
        }

        return actions;
    }

    private static NpcLocation parseLocation(String serializedLocation) {
        String[] parts = serializedLocation.split(",");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Teleport format: world,x,y,z,yaw,pitch");
        }
        return new NpcLocation(
                parts[0].trim(),
                parseDouble(parts[1].trim(), "teleport x"),
                parseDouble(parts[2].trim(), "teleport y"),
                parseDouble(parts[3].trim(), "teleport z"),
                parseFloat(parts[4].trim(), "teleport yaw"),
                parseFloat(parts[5].trim(), "teleport pitch")
        );
    }

    private static Map<String, String> parseAttributes(String serializedAttributes) {
        Map<String, String> attributes = new HashMap<>();
        if (serializedAttributes == null || serializedAttributes.isBlank()) {
            return attributes;
        }
        for (String token : serializedAttributes.split(";")) {
            String[] pair = token.split(":", 2);
            if (pair.length != 2) {
                continue;
            }
            String key = pair[0].trim();
            String value = pair[1].trim();
            if (!key.isBlank()) {
                attributes.put(key, value);
            }
        }
        return attributes;
    }

    private static EndpointResponse badRequest(String message) {
        return new EndpointResponse(400, "{\"error\":\"" + escape(message) + "\"}", Map.of());
    }

    private static String toJson(NpcDefinition definition) {
        StringBuilder builder = new StringBuilder();
        builder.append('{')
                .append("\"id\":\"").append(escape(definition.id())).append("\",")
                .append("\"name\":\"").append(escape(definition.displayName())).append("\",")
                .append("\"type\":\"").append(definition.type().name()).append("\",")
                .append("\"world\":\"").append(escape(definition.location().world())).append("\",")
                .append("\"x\":").append(definition.location().x()).append(',')
                .append("\"y\":").append(definition.location().y()).append(',')
                .append("\"z\":").append(definition.location().z()).append(',')
                .append("\"yaw\":").append(definition.location().yaw()).append(',')
                .append("\"pitch\":").append(definition.location().pitch()).append(',')
                .append("\"actions\":").append(definition.actions().size())
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

    private static NpcType parseNpcType(String rawType) {
        try {
            return NpcType.valueOf(rawType.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown NPC type: " + rawType, exception);
        }
    }

    private static NpcInteractionType parseInteractionType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            return NpcInteractionType.INTERACT;
        }
        try {
            return NpcInteractionType.valueOf(rawType.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return NpcInteractionType.INTERACT;
        }
    }

    private static double parseDouble(String value, String field) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid double for " + field + ": " + value, exception);
        }
    }

    private static float parseFloat(String value, String field) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid float for " + field + ": " + value, exception);
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
