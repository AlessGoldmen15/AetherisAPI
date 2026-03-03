package fr.aetheris.api.domain.gui.bukkit;

import fr.aetheris.api.domain.gui.GuiDefinition;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class AetherisGuiHolder implements InventoryHolder {

    private final GuiDefinition guiDefinition;
    private Inventory inventory;

    public AetherisGuiHolder(GuiDefinition guiDefinition) {
        this.guiDefinition = guiDefinition;
    }

    public GuiDefinition guiDefinition() {
        return guiDefinition;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
