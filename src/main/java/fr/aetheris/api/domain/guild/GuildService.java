package fr.aetheris.api.domain.guild;

import fr.aetheris.api.service.AetherisService;
import java.util.Optional;

public interface GuildService extends AetherisService {

    Guild create(String guildId, String name, String ownerId);

    Optional<Guild> byId(String guildId);

    void addMember(String guildId, String memberId);

    void removeMember(String guildId, String memberId);
}
