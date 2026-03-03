package fr.aetheris.api.domain.npc;

import java.util.List;

public interface NpcExecutionAdapter {

    void teleport(String playerId, NpcLocation destination);

    void applyStat(String playerId, String statKey, double value, StatOperation operation);

    void grantLoot(String playerId, NpcLootEntry entry, int amount);

    void openMerchant(String playerId, List<NpcMerchantOffer> offers);

    void startDialogue(String playerId, String dialogueId, List<String> lines);

    void handleCustomAction(String playerId, NpcAction action);
}
