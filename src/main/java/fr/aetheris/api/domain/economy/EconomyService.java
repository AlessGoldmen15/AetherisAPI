package fr.aetheris.api.domain.economy;

import fr.aetheris.api.service.AetherisService;

public interface EconomyService extends AetherisService {

    double balanceOf(String accountId);

    void deposit(String accountId, double amount);

    boolean withdraw(String accountId, double amount);
}
