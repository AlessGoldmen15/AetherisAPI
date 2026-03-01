package fr.aetheris.api.command;

import fr.aetheris.api.security.PermissionService;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CommandDispatcher {

    private final CommandRegistry commandRegistry;
    private final PermissionService permissionService;

    public CommandDispatcher(CommandRegistry commandRegistry, PermissionService permissionService) {
        this.commandRegistry = Objects.requireNonNull(commandRegistry, "commandRegistry must not be null");
        this.permissionService = Objects.requireNonNull(permissionService, "permissionService must not be null");
    }

    public CommandResult dispatch(String senderId, String commandName, List<String> args, Map<String, Object> metadata) {
        final CommandContext context = new CommandContext(
                senderId,
                args,
                metadata,
                permission -> permissionService.hasPermission(senderId, permission)
        );
        return commandRegistry.execute(commandName, context);
    }
}
