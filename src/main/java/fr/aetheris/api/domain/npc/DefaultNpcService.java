package fr.aetheris.api.domain.npc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultNpcService implements NpcService {

    private final Map<String, NpcDefinition> npcsById = new ConcurrentHashMap<>();

    @Override
    public NpcDefinition save(NpcDefinition npcDefinition) {
        npcsById.put(npcDefinition.id(), npcDefinition);
        return npcDefinition;
    }

    @Override
    public Optional<NpcDefinition> byId(String npcId) {
        return Optional.ofNullable(npcsById.get(npcId));
    }

    @Override
    public List<NpcDefinition> byType(NpcType type) {
        List<NpcDefinition> matches = new ArrayList<>();
        for (NpcDefinition npc : npcsById.values()) {
            if (npc.type() == type) {
                matches.add(npc);
            }
        }
        return List.copyOf(matches);
    }

    @Override
    public List<NpcDefinition> all() {
        return List.copyOf(npcsById.values());
    }

    @Override
    public NpcInteractionResult interact(String npcId, NpcInteractionContext context) {
        NpcDefinition npc = npcsById.get(npcId);
        if (npc == null) {
            return new NpcInteractionResult(false, "NPC not found", List.of());
        }

        return new NpcInteractionResult(true, "interaction accepted", npc.actions());
    }

    @Override
    public boolean delete(String npcId) {
        return npcsById.remove(npcId) != null;
    }
}
