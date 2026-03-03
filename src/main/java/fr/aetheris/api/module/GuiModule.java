package fr.aetheris.api.module;

import fr.aetheris.api.AetherisApi;
import fr.aetheris.api.domain.gui.GuiEndpoints;
import fr.aetheris.api.domain.gui.GuiService;
import fr.aetheris.api.domain.gui.PersistentGuiService;

public final class GuiModule implements AetherisModule {

    private final String namespace;
    private final String managePermission;
    private final String usePermission;

    public GuiModule() {
        this("guis", "gui.manage", "gui.use");
    }

    public GuiModule(String namespace, String managePermission, String usePermission) {
        this.namespace = namespace;
        this.managePermission = managePermission;
        this.usePermission = usePermission;
    }

    @Override
    public void register(AetherisApi api) {
        GuiService guiService = new PersistentGuiService(api.storage().open(namespace));
        api.services().register(GuiService.class, guiService);
        GuiEndpoints.registerCrud(guiService, api.endpoints(), managePermission, usePermission);
    }
}
