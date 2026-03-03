package fr.aetheris.api.domain.npc;

import java.util.List;

public record NpcInteractionResult(
        boolean success,
        String message,
        List<NpcAction> actions
) {
}
