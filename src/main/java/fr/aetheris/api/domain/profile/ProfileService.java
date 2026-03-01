package fr.aetheris.api.domain.profile;

import fr.aetheris.api.service.AetherisService;
import java.util.Optional;

public interface ProfileService extends AetherisService {

    Optional<PlayerProfile> byPlayerId(String playerId);

    PlayerProfile create(String playerId);

    void setAttribute(String playerId, String key, String value);
}
