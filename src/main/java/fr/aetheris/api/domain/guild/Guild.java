package fr.aetheris.api.domain.guild;

import java.util.Set;

public record Guild(
        String id,
        String name,
        String ownerId,
        Set<String> members
) {
}
