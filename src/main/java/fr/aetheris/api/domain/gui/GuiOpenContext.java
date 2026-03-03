package fr.aetheris.api.domain.gui;

import java.util.Map;

public record GuiOpenContext(
        String playerId,
        Map<String, String> context
) {
}
