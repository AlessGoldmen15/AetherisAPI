package fr.aetheris.api.domain.gui;

public record GuiButton(
        int slot,
        String label,
        String icon,
        GuiActionType actionType,
        String actionPayload,
        boolean closeOnClick
) {
}
