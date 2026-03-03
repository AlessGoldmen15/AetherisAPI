package fr.aetheris.api.domain.gui;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record GuiDefinition(
        String id,
        String title,
        GuiType type,
        int rows,
        int size,
        String requiredPermission,
        List<GuiButton> buttons,
        Map<String, String> metadata
) {
    public GuiDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(type, "type");
        buttons = buttons == null ? List.of() : List.copyOf(buttons);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
