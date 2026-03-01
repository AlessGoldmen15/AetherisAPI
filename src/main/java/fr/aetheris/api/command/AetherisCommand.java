package fr.aetheris.api.command;

import java.util.List;

public record AetherisCommand(
        String name,
        String description,
        String requiredPermission,
        List<String> aliases,
        CommandExecutor executor
) {
}
