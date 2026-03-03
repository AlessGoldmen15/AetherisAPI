package fr.aetheris.api.domain.npc;

public record NpcLootEntry(
        String itemId,
        int minAmount,
        int maxAmount,
        double chance
) {
}
