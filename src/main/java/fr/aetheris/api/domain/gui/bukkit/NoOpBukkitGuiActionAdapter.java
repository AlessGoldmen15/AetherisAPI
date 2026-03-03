package fr.aetheris.api.domain.gui.bukkit;

import fr.aetheris.api.domain.gui.GuiButton;
import fr.aetheris.api.domain.gui.GuiDefinition;
import org.bukkit.entity.Player;

public final class NoOpBukkitGuiActionAdapter implements BukkitGuiActionAdapter {

    @Override
    public void onEndpoint(Player player, GuiDefinition gui, GuiButton button, String payload) {
    }

    @Override
    public void onNpcInteract(Player player, GuiDefinition gui, GuiButton button, String payload) {
    }

    @Override
    public void onCustom(Player player, GuiDefinition gui, GuiButton button, String payload) {
    }
}
