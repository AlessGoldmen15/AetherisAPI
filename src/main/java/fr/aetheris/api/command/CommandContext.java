package fr.aetheris.api.command;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public record CommandContext(
        String senderId,
        List<String> args,
        Map<String, Object> metadata,
        Function<String, Boolean> permissionChecker
) {

    public boolean hasPermission(String permission) {
        return permissionChecker != null && permissionChecker.apply(permission);
    }
}
