package fr.aetheris.api.domain.gui;

import fr.aetheris.api.service.AetherisService;
import java.util.List;
import java.util.Optional;

public interface GuiService extends AetherisService {

    GuiDefinition save(GuiDefinition definition);

    Optional<GuiDefinition> byId(String guiId);

    List<GuiDefinition> all();

    boolean delete(String guiId);

    GuiRenderResult open(String guiId, GuiOpenContext context);
}
