package fr.aetheris.api.domain.gui;

public record GuiRenderResult(
        boolean success,
        String message,
        GuiDefinition gui
) {
}
