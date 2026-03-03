package fr.aetheris.api.domain.npc;

import java.util.List;

public record DialogueAction(
        String dialogueId,
        List<String> lines
) implements NpcAction {
}
