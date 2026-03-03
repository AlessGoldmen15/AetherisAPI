package fr.aetheris.api.domain.npc;

import fr.aetheris.api.service.AetherisService;
import java.util.List;
import java.util.Optional;

public interface NpcService extends AetherisService {

    NpcDefinition save(NpcDefinition npcDefinition);

    Optional<NpcDefinition> byId(String npcId);

    List<NpcDefinition> byType(NpcType type);

    List<NpcDefinition> all();

    NpcInteractionResult interact(String npcId, NpcInteractionContext context);

    boolean delete(String npcId);
}
