package fr.aetheris.api.command;

import java.util.Collection;
import java.util.Optional;

public interface CommandRegistry {

    void register(AetherisCommand command);

    Optional<AetherisCommand> find(String commandNameOrAlias);

    CommandResult execute(String commandNameOrAlias, CommandContext context);

    Collection<AetherisCommand> all();

    void clear();
}
