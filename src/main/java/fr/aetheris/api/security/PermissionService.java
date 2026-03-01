package fr.aetheris.api.security;

public interface PermissionService {

    void grant(String subjectId, String permission);

    void revoke(String subjectId, String permission);

    boolean hasPermission(String subjectId, String permission);

    void clear();
}
