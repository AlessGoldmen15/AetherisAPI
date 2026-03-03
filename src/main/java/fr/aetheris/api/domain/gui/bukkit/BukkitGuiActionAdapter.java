package fr.aetheris.api.domain.gui.bukkit;

import fr.aetheris.api.domain.gui.GuiButton;
import fr.aetheris.api.domain.gui.GuiDefinition;
import org.bukkit.entity.Player;

public interface BukkitGuiActionAdapter {

    void onEndpoint(Player player, GuiDefinition gui, GuiButton button, String payload);

    void onNpcInteract(Player player, GuiDefinition gui, GuiButton button, String payload);

    void onCustom(Player player, GuiDefinition gui, GuiButton button, String payload);
}
