package fr.aetheris.api.domain.gui;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public final class GuiCodec {

    public String serialize(GuiDefinition gui) {
        Properties properties = new Properties();
        properties.setProperty("id", gui.id());
        properties.setProperty("title", gui.title());
        properties.setProperty("type", gui.type().name());
        properties.setProperty("rows", String.valueOf(gui.rows()));
        properties.setProperty("size", String.valueOf(gui.size()));
        properties.setProperty("requiredPermission", gui.requiredPermission() == null ? "" : gui.requiredPermission());

        Map<String, String> metadata = new TreeMap<>(gui.metadata());
        properties.setProperty("metadata.count", String.valueOf(metadata.size()));
        int metadataIndex = 0;
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            properties.setProperty("metadata." + metadataIndex + ".key", entry.getKey());
            properties.setProperty("metadata." + metadataIndex + ".value", entry.getValue());
            metadataIndex++;
        }

        List<GuiButton> buttons = gui.buttons();
        properties.setProperty("buttons.count", String.valueOf(buttons.size()));
        for (int index = 0; index < buttons.size(); index++) {
            GuiButton button = buttons.get(index);
            String prefix = "buttons." + index;
            properties.setProperty(prefix + ".slot", String.valueOf(button.slot()));
            properties.setProperty(prefix + ".label", button.label());
            properties.setProperty(prefix + ".icon", button.icon() == null ? "" : button.icon());
            properties.setProperty(prefix + ".actionType", button.actionType().name());
            properties.setProperty(prefix + ".actionPayload", button.actionPayload() == null ? "" : button.actionPayload());
            properties.setProperty(prefix + ".closeOnClick", String.valueOf(button.closeOnClick()));
        }

        try {
            StringWriter writer = new StringWriter();
            properties.store(writer, "Aetheris gui");
            return writer.toString();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to serialize gui " + gui.id(), exception);
        }
    }

    public GuiDefinition deserialize(String raw) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(raw));
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to deserialize GUI payload", exception);
        }

        String id = required(properties, "id");
        String title = required(properties, "title");
        GuiType type = GuiType.valueOf(required(properties, "type"));
        int rows = parseIntOrDefault(properties.getProperty("rows"), 0);
        int size = parseIntOrDefault(properties.getProperty("size"), 0);
        String requiredPermission = properties.getProperty("requiredPermission");
        if (requiredPermission != null && requiredPermission.isBlank()) {
            requiredPermission = null;
        }

        int metadataCount = parseIntOrDefault(properties.getProperty("metadata.count"), 0);
        Map<String, String> metadata = new HashMap<>();
        for (int metadataIndex = 0; metadataIndex < metadataCount; metadataIndex++) {
            String key = properties.getProperty("metadata." + metadataIndex + ".key");
            String value = properties.getProperty("metadata." + metadataIndex + ".value", "");
            if (key != null && !key.isBlank()) {
                metadata.put(key, value);
            }
        }

        int buttonCount = parseIntOrDefault(properties.getProperty("buttons.count"), 0);
        List<GuiButton> buttons = new ArrayList<>();
        for (int index = 0; index < buttonCount; index++) {
            String prefix = "buttons." + index;
            buttons.add(new GuiButton(
                    parseInt(properties, prefix + ".slot"),
                    required(properties, prefix + ".label"),
                    properties.getProperty(prefix + ".icon"),
                    GuiActionType.valueOf(required(properties, prefix + ".actionType")),
                    properties.getProperty(prefix + ".actionPayload"),
                    Boolean.parseBoolean(properties.getProperty(prefix + ".closeOnClick", "false"))
            ));
        }

        return new GuiDefinition(id, title, type, rows, size, requiredPermission, buttons, metadata);
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required gui field: " + key);
        }
        return value;
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
