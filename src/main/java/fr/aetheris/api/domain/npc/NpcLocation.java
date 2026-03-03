package fr.aetheris.api.domain.npc;

public record NpcLocation(
        String world,
        double x,
        double y,
        double z,
        float yaw,
        float pitch
) {
}
