package fr.aetheris.api.domain.npc;

import java.util.List;

public record MerchantAction(
        List<NpcMerchantOffer> offers
) implements NpcAction {
}
