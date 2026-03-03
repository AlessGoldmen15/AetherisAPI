package fr.aetheris.api.domain.gui.bukkit;

import fr.aetheris.api.AetherisApi;
import fr.aetheris.api.domain.gui.GuiActionType;
import fr.aetheris.api.domain.gui.GuiButton;
import fr.aetheris.api.domain.gui.GuiDefinition;
import fr.aetheris.api.domain.gui.GuiOpenContext;
import fr.aetheris.api.domain.gui.GuiRenderResult;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class BukkitGuiBridge implements Listener {

    private final AetherisApi api;
    private final JavaPlugin plugin;
    private final BukkitGuiActionAdapter actionAdapter;

    public BukkitGuiBridge(AetherisApi api, JavaPlugin plugin) {
        this(api, plugin, new NoOpBukkitGuiActionAdapter());
    }

    public BukkitGuiBridge(AetherisApi api, JavaPlugin plugin, BukkitGuiActionAdapter actionAdapter) {
        this.api = api;
        this.plugin = plugin;
        this.actionAdapter = actionAdapter;
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public boolean open(Player player, String guiId) {
        return open(player, guiId, Map.of());
    }

    public boolean open(Player player, String guiId, Map<String, String> context) {
        GuiRenderResult renderResult = api.guis().open(guiId, new GuiOpenContext(player.getUniqueId().toString(), context));
        if (!renderResult.success() || renderResult.gui() == null) {
            return false;
        }

        GuiDefinition gui = renderResult.gui();
        if (gui.requiredPermission() != null && !gui.requiredPermission().isBlank()) {
            if (!api.permissions().hasPermission(player.getUniqueId().toString(), gui.requiredPermission())) {
                return false;
            }
        }

        Inventory inventory = createInventory(gui, player, context);
        player.openInventory(inventory);
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!(event.getInventory().getHolder() instanceof AetherisGuiHolder holder)) {
            return;
        }

        event.setCancelled(true);

        GuiDefinition gui = holder.guiDefinition();
        GuiButton button = findButton(gui, event.getRawSlot());
        if (button == null) {
            return;
        }

        executeButton(player, gui, button);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof AetherisGuiHolder holder)) {
            return;
        }
        holder.setInventory(null);
    }

    private Inventory createInventory(GuiDefinition gui, Player player, Map<String, String> context) {
        AetherisGuiHolder holder = new AetherisGuiHolder(gui);
        String title = colorize(applyContext(gui.title(), player, context));
        Inventory inventory = switch (gui.type()) {
            case HOPPER -> Bukkit.createInventory(holder, InventoryType.HOPPER, title);
            case ANVIL -> Bukkit.createInventory(holder, InventoryType.ANVIL, title);
            default -> Bukkit.createInventory(holder, normalizeSize(gui), title);
        };
        holder.setInventory(inventory);

        for (GuiButton button : gui.buttons()) {
            if (button.slot() < 0 || button.slot() >= inventory.getSize()) {
                continue;
            }
            inventory.setItem(button.slot(), toItemStack(button, player, context));
        }
        return inventory;
    }

    private int normalizeSize(GuiDefinition gui) {
        if (gui.size() > 0) {
            int size = Math.min(54, gui.size());
            int remainder = size % 9;
            if (remainder == 0) {
                return size;
            }
            return Math.min(54, size + (9 - remainder));
        }
        if (gui.rows() > 0) {
            return Math.max(9, Math.min(54, gui.rows() * 9));
        }
        return 27;
    }

    private ItemStack toItemStack(GuiButton button, Player player, Map<String, String> context) {
        Material material = parseMaterial(button.icon()).orElse(Material.PAPER);
        ItemStack itemStack = new ItemStack(material);

        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(colorize(applyContext(button.label(), player, context)));
            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

    private GuiButton findButton(GuiDefinition gui, int slot) {
        for (GuiButton button : gui.buttons()) {
            if (button.slot() == slot) {
                return button;
            }
        }
        return null;
    }

    private void executeButton(Player player, GuiDefinition gui, GuiButton button) {
        String payload = button.actionPayload() == null ? "" : button.actionPayload();
        switch (button.actionType()) {
            case COMMAND -> executeCommand(player, payload);
            case TELEPORT -> executeTeleport(player, payload);
            case ENDPOINT -> actionAdapter.onEndpoint(player, gui, button, payload);
            case NPC_INTERACT -> actionAdapter.onNpcInteract(player, gui, button, payload);
            case CLOSE -> player.closeInventory();
            case CUSTOM -> actionAdapter.onCustom(player, gui, button, payload);
        }

        if (button.closeOnClick() && button.actionType() != GuiActionType.CLOSE) {
            player.closeInventory();
        }
    }

    private void executeCommand(Player player, String payload) {
        if (payload == null || payload.isBlank()) {
            return;
        }
        String command = payload.startsWith("/") ? payload.substring(1) : payload;
        player.performCommand(command);
    }

    private void executeTeleport(Player player, String payload) {
        if (payload == null || payload.isBlank()) {
            return;
        }

        String[] parts = payload.split(",");
        if (parts.length < 4) {
            return;
        }

        World world = Bukkit.getWorld(parts[0].trim());
        if (world == null) {
            return;
        }

        try {
            double x = Double.parseDouble(parts[1].trim());
            double y = Double.parseDouble(parts[2].trim());
            double z = Double.parseDouble(parts[3].trim());
            float yaw = parts.length >= 5 ? Float.parseFloat(parts[4].trim()) : player.getLocation().getYaw();
            float pitch = parts.length >= 6 ? Float.parseFloat(parts[5].trim()) : player.getLocation().getPitch();
            player.teleport(new Location(world, x, y, z, yaw, pitch));
        } catch (NumberFormatException ignored) {
        }
    }

    private Optional<Material> parseMaterial(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(Material.matchMaterial(raw.trim()));
    }

    private String applyContext(String value, Player player, Map<String, String> context) {
        if (value == null) {
            return "";
        }

        Map<String, String> tokens = new HashMap<>(context);
        tokens.put("player", player.getName());
        tokens.put("uuid", player.getUniqueId().toString());

        String resolved = value;
        for (Map.Entry<String, String> entry : tokens.entrySet()) {
            resolved = resolved.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return resolved;
    }

    private String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
