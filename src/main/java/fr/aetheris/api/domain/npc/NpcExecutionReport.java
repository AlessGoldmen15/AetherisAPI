package fr.aetheris.api.domain.npc;

import java.util.List;

public record NpcExecutionReport(
        int executedActions,
        List<String> log
) {
}
