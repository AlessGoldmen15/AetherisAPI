package fr.aetheris.api.domain.npc;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record NpcDefinition(
        String id,
        String displayName,
        NpcType type,
        NpcLocation location,
        Map<String, String> attributes,
        List<NpcAction> actions
) {
    public NpcDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(location, "location");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        actions = actions == null ? List.of() : List.copyOf(actions);
    }
}
