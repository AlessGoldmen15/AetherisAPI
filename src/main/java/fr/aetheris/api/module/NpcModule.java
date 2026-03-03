package fr.aetheris.api.module;

import fr.aetheris.api.AetherisApi;
import fr.aetheris.api.domain.npc.NpcEndpoints;
import fr.aetheris.api.domain.npc.NpcService;
import fr.aetheris.api.domain.npc.PersistentNpcService;

public final class NpcModule implements AetherisModule {

    private final String namespace;
    private final String managePermission;
    private final String usePermission;

    public NpcModule() {
        this("npcs", "npc.manage", "npc.use");
    }

    public NpcModule(String namespace, String managePermission, String usePermission) {
        this.namespace = namespace;
        this.managePermission = managePermission;
        this.usePermission = usePermission;
    }

    @Override
    public void register(AetherisApi api) {
        NpcService npcService = new PersistentNpcService(api.storage().open(namespace));
        api.services().register(NpcService.class, npcService);
        NpcEndpoints.registerCrud(npcService, api.endpoints(), managePermission, usePermission);
    }
}
