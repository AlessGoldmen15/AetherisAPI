package fr.aetheris.api.domain.npc;

import java.util.List;

public record LootAction(
        List<NpcLootEntry> entries
) implements NpcAction {
}
