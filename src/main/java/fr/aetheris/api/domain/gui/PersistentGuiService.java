package fr.aetheris.api.domain.gui;

import fr.aetheris.api.storage.KeyValueStore;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class PersistentGuiService implements GuiService {

    private final KeyValueStore store;
    private final GuiCodec codec;
    private final Map<String, GuiDefinition> guis = new ConcurrentHashMap<>();

    public PersistentGuiService(KeyValueStore store) {
        this(store, new GuiCodec());
    }

    public PersistentGuiService(KeyValueStore store, GuiCodec codec) {
        this.store = store;
        this.codec = codec;
        loadExisting();
    }

    @Override
    public GuiDefinition save(GuiDefinition definition) {
        guis.put(definition.id(), definition);
        store.put(definition.id(), codec.serialize(definition));
        return definition;
    }

    @Override
    public Optional<GuiDefinition> byId(String guiId) {
        return Optional.ofNullable(guis.get(guiId));
    }

    @Override
    public List<GuiDefinition> all() {
        return List.copyOf(guis.values());
    }

    @Override
    public boolean delete(String guiId) {
        GuiDefinition removed = guis.remove(guiId);
        if (removed == null) {
            return false;
        }
        store.delete(guiId);
        return true;
    }

    @Override
    public GuiRenderResult open(String guiId, GuiOpenContext context) {
        GuiDefinition definition = guis.get(guiId);
        if (definition == null) {
            return new GuiRenderResult(false, "GUI not found", null);
        }
        return new GuiRenderResult(true, "gui ready", definition);
    }

    private void loadExisting() {
        for (String key : store.keys()) {
            store.get(key).ifPresent(raw -> {
                try {
                    guis.put(key, codec.deserialize(raw));
                } catch (RuntimeException ignored) {
                }
            });
        }
    }
}
