package fr.aetheris.api.domain.npc;

public record NpcMerchantOffer(
        String itemId,
        int amount,
        double price,
        String currency
) {
}
