package fr.aetheris.api.domain.npc;

public record TeleportAction(
        NpcLocation destination
) implements NpcAction {
}
