package fr.aetheris.api.domain.npc;

import fr.aetheris.api.storage.KeyValueStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class PersistentNpcService implements NpcService {

    private final KeyValueStore store;
    private final NpcCodec codec;
    private final Map<String, NpcDefinition> npcsById = new ConcurrentHashMap<>();

    public PersistentNpcService(KeyValueStore store) {
        this(store, new NpcCodec());
    }

    public PersistentNpcService(KeyValueStore store, NpcCodec codec) {
        this.store = store;
        this.codec = codec;
        loadExisting();
    }

    @Override
    public NpcDefinition save(NpcDefinition npcDefinition) {
        npcsById.put(npcDefinition.id(), npcDefinition);
        store.put(npcDefinition.id(), codec.serialize(npcDefinition));
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
        NpcDefinition removed = npcsById.remove(npcId);
        if (removed == null) {
            return false;
        }
        store.delete(npcId);
        return true;
    }

    private void loadExisting() {
        for (String key : store.keys()) {
            store.get(key).ifPresent(raw -> {
                try {
                    npcsById.put(key, codec.deserialize(raw));
                } catch (RuntimeException ignored) {
                }
            });
        }
    }
}
