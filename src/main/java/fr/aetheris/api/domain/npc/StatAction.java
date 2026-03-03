package fr.aetheris.api.domain.npc;

public record StatAction(
        String statKey,
        double value,
        StatOperation operation
) implements NpcAction {
}
