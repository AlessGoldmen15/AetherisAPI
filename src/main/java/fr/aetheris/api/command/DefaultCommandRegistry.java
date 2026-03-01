package fr.aetheris.api.command;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultCommandRegistry implements CommandRegistry {

    private final Map<String, AetherisCommand> commands = new ConcurrentHashMap<>();
    private final Map<String, String> aliases = new ConcurrentHashMap<>();

    @Override
    public void register(AetherisCommand command) {
        validate(command);
        final String normalizedName = normalize(command.name());
        commands.put(normalizedName, command);
        for (String alias : command.aliases()) {
            aliases.put(normalize(alias), normalizedName);
        }
    }

    @Override
    public Optional<AetherisCommand> find(String commandNameOrAlias) {
        if (commandNameOrAlias == null || commandNameOrAlias.isBlank()) {
            return Optional.empty();
        }
        final String normalized = normalize(commandNameOrAlias);
        final String canonical = aliases.getOrDefault(normalized, normalized);
        return Optional.ofNullable(commands.get(canonical));
    }

    @Override
    public CommandResult execute(String commandNameOrAlias, CommandContext context) {
        return find(commandNameOrAlias)
                .map(command -> executeCommand(command, context))
                .orElse(CommandResult.NOT_FOUND);
    }

    @Override
    public Collection<AetherisCommand> all() {
        return Collections.unmodifiableCollection(commands.values());
    }

    @Override
    public void clear() {
        commands.clear();
        aliases.clear();
    }

    private static String normalize(String value) {
        return value.toLowerCase().trim();
    }

    private static void validate(AetherisCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.name() == null || command.name().isBlank()) {
            throw new IllegalArgumentException("command name must not be blank");
        }
        if (command.aliases() == null) {
            throw new IllegalArgumentException("command aliases must not be null");
        }
        if (command.executor() == null) {
            throw new IllegalArgumentException("command executor must not be null");
        }
    }

    private static CommandResult executeCommand(AetherisCommand command, CommandContext context) {
        final String requiredPermission = command.requiredPermission();
        if (requiredPermission != null && !requiredPermission.isBlank() && !context.hasPermission(requiredPermission)) {
            return CommandResult.NO_PERMISSION;
        }
        return command.executor().execute(context);
    }
}
