package fr.aetheris.api.domain.gui;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultGuiService implements GuiService {

    private final Map<String, GuiDefinition> guis = new ConcurrentHashMap<>();

    @Override
    public GuiDefinition save(GuiDefinition definition) {
        guis.put(definition.id(), definition);
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
        return guis.remove(guiId) != null;
    }

    @Override
    public GuiRenderResult open(String guiId, GuiOpenContext context) {
        GuiDefinition definition = guis.get(guiId);
        if (definition == null) {
            return new GuiRenderResult(false, "GUI not found", null);
        }
        return new GuiRenderResult(true, "gui ready", definition);
    }
}
