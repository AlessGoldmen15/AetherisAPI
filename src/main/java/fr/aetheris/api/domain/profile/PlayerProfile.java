package fr.aetheris.api.domain.profile;

import java.util.Map;

public record PlayerProfile(
        String playerId,
        Map<String, String> attributes
) {
}
