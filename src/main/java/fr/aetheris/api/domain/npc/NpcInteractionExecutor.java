package fr.aetheris.api.domain.npc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class NpcInteractionExecutor {

    private final Random random;

    public NpcInteractionExecutor() {
        this(new Random());
    }

    public NpcInteractionExecutor(Random random) {
        this.random = random;
    }

    public NpcExecutionReport execute(String playerId, NpcInteractionResult result, NpcExecutionAdapter adapter) {
        if (result == null || !result.success()) {
            return new NpcExecutionReport(0, List.of());
        }

        int executedActions = 0;
        List<String> log = new ArrayList<>();
        for (NpcAction action : result.actions()) {
            if (action instanceof TeleportAction teleportAction) {
                adapter.teleport(playerId, teleportAction.destination());
                executedActions++;
                log.add("teleport");
                continue;
            }
            if (action instanceof StatAction statAction) {
                adapter.applyStat(playerId, statAction.statKey(), statAction.value(), statAction.operation());
                executedActions++;
                log.add("stat:" + statAction.statKey());
                continue;
            }
            if (action instanceof LootAction lootAction) {
                executeLoot(playerId, adapter, lootAction, log);
                executedActions++;
                continue;
            }
            if (action instanceof MerchantAction merchantAction) {
                adapter.openMerchant(playerId, merchantAction.offers());
                executedActions++;
                log.add("merchant");
                continue;
            }
            if (action instanceof DialogueAction dialogueAction) {
                adapter.startDialogue(playerId, dialogueAction.dialogueId(), dialogueAction.lines());
                executedActions++;
                log.add("dialogue:" + dialogueAction.dialogueId());
                continue;
            }
            adapter.handleCustomAction(playerId, action);
            executedActions++;
            log.add("custom");
        }

        return new NpcExecutionReport(executedActions, List.copyOf(log));
    }

    private void executeLoot(String playerId, NpcExecutionAdapter adapter, LootAction lootAction, List<String> log) {
        for (NpcLootEntry entry : lootAction.entries()) {
            if (random.nextDouble() > entry.chance()) {
                continue;
            }
            int min = Math.max(0, entry.minAmount());
            int max = Math.max(min, entry.maxAmount());
            int amount = min;
            if (max > min) {
                amount = random.nextInt(max - min + 1) + min;
            }
            if (amount <= 0) {
                continue;
            }
            adapter.grantLoot(playerId, entry, amount);
            log.add("loot:" + entry.itemId() + "x" + amount);
        }
    }
}
