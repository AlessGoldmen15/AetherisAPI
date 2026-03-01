package fr.aetheris.api.security;

import java.util.Set;

public record Role(
        String name,
        Set<String> permissions,
        Set<String> parents
) {
}
