package fr.aetheris.api;

import fr.aetheris.api.command.CommandRegistry;
import fr.aetheris.api.domain.gui.GuiService;
import fr.aetheris.api.domain.npc.NpcService;
import fr.aetheris.api.endpoint.EndpointRegistry;
import fr.aetheris.api.event.EventBus;
import fr.aetheris.api.security.PermissionService;
import fr.aetheris.api.security.RoleService;
import fr.aetheris.api.service.ServiceRegistry;
import fr.aetheris.api.storage.StorageManager;

public interface AetherisApi extends AutoCloseable {

    ServiceRegistry services();

    CommandRegistry commands();

    EventBus events();

    EndpointRegistry endpoints();

    PermissionService permissions();

    RoleService roles();

    StorageManager storage();

    default GuiService guis() {
        return services().require(GuiService.class);
    }

    default NpcService npcs() {
        return services().require(NpcService.class);
    }

    @Override
    void close();
}
