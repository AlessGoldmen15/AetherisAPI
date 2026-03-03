package fr.aetheris.api.domain.npc;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public final class NpcCodec {

    public String serialize(NpcDefinition npc) {
        Properties properties = new Properties();
        properties.setProperty("id", npc.id());
        properties.setProperty("displayName", npc.displayName());
        properties.setProperty("type", npc.type().name());

        NpcLocation location = npc.location();
        properties.setProperty("location.world", location.world());
        properties.setProperty("location.x", String.valueOf(location.x()));
        properties.setProperty("location.y", String.valueOf(location.y()));
        properties.setProperty("location.z", String.valueOf(location.z()));
        properties.setProperty("location.yaw", String.valueOf(location.yaw()));
        properties.setProperty("location.pitch", String.valueOf(location.pitch()));

        Map<String, String> attributes = new TreeMap<>(npc.attributes());
        properties.setProperty("attributes.count", String.valueOf(attributes.size()));
        int attributeIndex = 0;
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            properties.setProperty("attributes." + attributeIndex + ".key", entry.getKey());
            properties.setProperty("attributes." + attributeIndex + ".value", entry.getValue());
            attributeIndex++;
        }

        List<NpcAction> actions = npc.actions();
        properties.setProperty("actions.count", String.valueOf(actions.size()));
        for (int actionIndex = 0; actionIndex < actions.size(); actionIndex++) {
            writeAction(properties, actionIndex, actions.get(actionIndex));
        }

        try {
            StringWriter writer = new StringWriter();
            properties.store(writer, "Aetheris npc");
            return writer.toString();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to serialize npc " + npc.id(), exception);
        }
    }

    public NpcDefinition deserialize(String raw) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(raw));
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to deserialize NPC payload", exception);
        }

        String id = required(properties, "id");
        String displayName = required(properties, "displayName");
        NpcType type = NpcType.valueOf(required(properties, "type"));

        NpcLocation location = new NpcLocation(
                required(properties, "location.world"),
                parseDouble(properties, "location.x"),
                parseDouble(properties, "location.y"),
                parseDouble(properties, "location.z"),
                parseFloat(properties, "location.yaw"),
                parseFloat(properties, "location.pitch")
        );

        int attributeCount = parseIntOrDefault(properties.getProperty("attributes.count"), 0);
        Map<String, String> attributes = new HashMap<>();
        for (int attributeIndex = 0; attributeIndex < attributeCount; attributeIndex++) {
            String key = properties.getProperty("attributes." + attributeIndex + ".key");
            String value = properties.getProperty("attributes." + attributeIndex + ".value", "");
            if (key != null && !key.isBlank()) {
                attributes.put(key, value);
            }
        }

        int actionCount = parseIntOrDefault(properties.getProperty("actions.count"), 0);
        List<NpcAction> actions = new ArrayList<>();
        for (int actionIndex = 0; actionIndex < actionCount; actionIndex++) {
            NpcAction action = readAction(properties, actionIndex);
            if (action != null) {
                actions.add(action);
            }
        }

        return new NpcDefinition(id, displayName, type, location, attributes, actions);
    }

    private void writeAction(Properties properties, int actionIndex, NpcAction action) {
        String prefix = "actions." + actionIndex;
        if (action instanceof TeleportAction teleportAction) {
            properties.setProperty(prefix + ".kind", "TELEPORT");
            NpcLocation destination = teleportAction.destination();
            properties.setProperty(prefix + ".destination.world", destination.world());
            properties.setProperty(prefix + ".destination.x", String.valueOf(destination.x()));
            properties.setProperty(prefix + ".destination.y", String.valueOf(destination.y()));
            properties.setProperty(prefix + ".destination.z", String.valueOf(destination.z()));
            properties.setProperty(prefix + ".destination.yaw", String.valueOf(destination.yaw()));
            properties.setProperty(prefix + ".destination.pitch", String.valueOf(destination.pitch()));
            return;
        }
        if (action instanceof StatAction statAction) {
            properties.setProperty(prefix + ".kind", "STAT");
            properties.setProperty(prefix + ".statKey", statAction.statKey());
            properties.setProperty(prefix + ".statValue", String.valueOf(statAction.value()));
            properties.setProperty(prefix + ".statOperation", statAction.operation().name());
            return;
        }
        if (action instanceof LootAction lootAction) {
            properties.setProperty(prefix + ".kind", "LOOT");
            List<NpcLootEntry> entries = lootAction.entries();
            properties.setProperty(prefix + ".entries.count", String.valueOf(entries.size()));
            for (int entryIndex = 0; entryIndex < entries.size(); entryIndex++) {
                NpcLootEntry entry = entries.get(entryIndex);
                String entryPrefix = prefix + ".entries." + entryIndex;
                properties.setProperty(entryPrefix + ".itemId", entry.itemId());
                properties.setProperty(entryPrefix + ".minAmount", String.valueOf(entry.minAmount()));
                properties.setProperty(entryPrefix + ".maxAmount", String.valueOf(entry.maxAmount()));
                properties.setProperty(entryPrefix + ".chance", String.valueOf(entry.chance()));
            }
            return;
        }
        if (action instanceof MerchantAction merchantAction) {
            properties.setProperty(prefix + ".kind", "MERCHANT");
            List<NpcMerchantOffer> offers = merchantAction.offers();
            properties.setProperty(prefix + ".offers.count", String.valueOf(offers.size()));
            for (int offerIndex = 0; offerIndex < offers.size(); offerIndex++) {
                NpcMerchantOffer offer = offers.get(offerIndex);
                String offerPrefix = prefix + ".offers." + offerIndex;
                properties.setProperty(offerPrefix + ".itemId", offer.itemId());
                properties.setProperty(offerPrefix + ".amount", String.valueOf(offer.amount()));
                properties.setProperty(offerPrefix + ".price", String.valueOf(offer.price()));
                properties.setProperty(offerPrefix + ".currency", offer.currency());
            }
            return;
        }
        if (action instanceof DialogueAction dialogueAction) {
            properties.setProperty(prefix + ".kind", "DIALOGUE");
            properties.setProperty(prefix + ".dialogueId", dialogueAction.dialogueId());
            List<String> lines = dialogueAction.lines();
            properties.setProperty(prefix + ".lines.count", String.valueOf(lines.size()));
            for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                properties.setProperty(prefix + ".lines." + lineIndex, lines.get(lineIndex));
            }
            return;
        }

        properties.setProperty(prefix + ".kind", "CUSTOM");
        properties.setProperty(prefix + ".class", action.getClass().getName());
    }

    private NpcAction readAction(Properties properties, int actionIndex) {
        String prefix = "actions." + actionIndex;
        String kind = properties.getProperty(prefix + ".kind");
        if (kind == null || kind.isBlank()) {
            return null;
        }

        if ("TELEPORT".equalsIgnoreCase(kind)) {
            return new TeleportAction(new NpcLocation(
                    required(properties, prefix + ".destination.world"),
                    parseDouble(properties, prefix + ".destination.x"),
                    parseDouble(properties, prefix + ".destination.y"),
                    parseDouble(properties, prefix + ".destination.z"),
                    parseFloat(properties, prefix + ".destination.yaw"),
                    parseFloat(properties, prefix + ".destination.pitch")
            ));
        }
        if ("STAT".equalsIgnoreCase(kind)) {
            return new StatAction(
                    required(properties, prefix + ".statKey"),
                    parseDouble(properties, prefix + ".statValue"),
                    StatOperation.valueOf(required(properties, prefix + ".statOperation"))
            );
        }
        if ("LOOT".equalsIgnoreCase(kind)) {
            int entryCount = parseIntOrDefault(properties.getProperty(prefix + ".entries.count"), 0);
            List<NpcLootEntry> entries = new ArrayList<>();
            for (int entryIndex = 0; entryIndex < entryCount; entryIndex++) {
                String entryPrefix = prefix + ".entries." + entryIndex;
                entries.add(new NpcLootEntry(
                        required(properties, entryPrefix + ".itemId"),
                        parseInt(properties, entryPrefix + ".minAmount"),
                        parseInt(properties, entryPrefix + ".maxAmount"),
                        parseDouble(properties, entryPrefix + ".chance")
                ));
            }
            return new LootAction(entries);
        }
        if ("MERCHANT".equalsIgnoreCase(kind)) {
            int offerCount = parseIntOrDefault(properties.getProperty(prefix + ".offers.count"), 0);
            List<NpcMerchantOffer> offers = new ArrayList<>();
            for (int offerIndex = 0; offerIndex < offerCount; offerIndex++) {
                String offerPrefix = prefix + ".offers." + offerIndex;
                offers.add(new NpcMerchantOffer(
                        required(properties, offerPrefix + ".itemId"),
                        parseInt(properties, offerPrefix + ".amount"),
                        parseDouble(properties, offerPrefix + ".price"),
                        required(properties, offerPrefix + ".currency")
                ));
            }
            return new MerchantAction(offers);
        }
        if ("DIALOGUE".equalsIgnoreCase(kind)) {
            int lineCount = parseIntOrDefault(properties.getProperty(prefix + ".lines.count"), 0);
            List<String> lines = new ArrayList<>();
            for (int lineIndex = 0; lineIndex < lineCount; lineIndex++) {
                lines.add(properties.getProperty(prefix + ".lines." + lineIndex, ""));
            }
            return new DialogueAction(required(properties, prefix + ".dialogueId"), lines);
        }

        return null;
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required npc field: " + key);
        }
        return value;
    }

    private static double parseDouble(Properties properties, String key) {
        String value = required(properties, key);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid numeric value for " + key + ": " + value, exception);
        }
    }

    private static float parseFloat(Properties properties, String key) {
        String value = required(properties, key);
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid numeric value for " + key + ": " + value, exception);
        }
    }

    private static int parseInt(Properties properties, String key) {
        String value = required(properties, key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid integer value for " + key + ": " + value, exception);
        }
    }

    private static int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }
}
