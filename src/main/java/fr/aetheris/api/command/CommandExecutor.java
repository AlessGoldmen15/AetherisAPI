package fr.aetheris.api.command;

@FunctionalInterface
public interface CommandExecutor {

    CommandResult execute(CommandContext context);
}
