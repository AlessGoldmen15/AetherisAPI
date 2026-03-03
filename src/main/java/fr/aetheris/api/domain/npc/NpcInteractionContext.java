package fr.aetheris.api.domain.npc;

import java.util.Map;

public record NpcInteractionContext(
        String playerId,
        NpcInteractionType interactionType,
        Map<String, String> context
) {
}
