# AetherisAPI

AetherisAPI est une librairie Java pour accélérer le développement de plugins Minecraft (Paper/Spigot).

Ce projet n'est pas un plugin autonome: c'est une API/framework que tu inclus dans tes plugins pour gérer rapidement:
- services (Economy, Guild, Profile, etc.)
- commandes
- events
- endpoints
- permissions et roles
- stockage (memoire, fichier, MySQL via JDBC, Mongo via adaptateur)

## Prerequis

- Java 17+
- Maven 3.8+

## Build

```bash
mvn clean package
```

Le JAR est genere dans `target/`.

## Installation dans un autre projet

Ajoute le JAR de cette API comme dependance dans ton plugin.

Exemple Maven (si publie sur un repo):

```xml
<dependency>
    <groupId>fr.aetheris.api</groupId>
    <artifactId>aetherisapi</artifactId>
    <version>1.1.0</version>
</dependency>
```

## Utilisation rapide

### 1) Classe principale de ton plugin

```java
import fr.aetheris.api.AetherisApi;
import fr.aetheris.api.AetherisPlugin;

public final class MyPlugin extends AetherisPlugin {
    @Override
    protected void configure(AetherisApi api) {
        // Enregistre services, roles, endpoints, etc.
    }

    @Override
    protected void onAetherisEnable(AetherisApi api) {
        // Startup
    }
}
```

### 2) Enregistrer un service metier

```java
api.services().register(EconomyService.class, new MyEconomyService());
```

### 3) Permissions et roles

```java
api.roles().createRole(new Role("admin", Set.of("eco.manage", "guild.manage"), Set.of()));
api.roles().assignRole("player-uuid", "admin");

boolean canManageEco = api.permissions().hasPermission("player-uuid", "eco.manage");
```

### 4) Commandes

```java
api.commands().register(new AetherisCommand(
        "money",
        "Afficher la balance",
        "eco.read",
        List.of("bal"),
        context -> CommandResult.SUCCESS
));
```

Puis dispatch:

```java
CommandDispatcher dispatcher = new CommandDispatcher(api.commands(), api.permissions());
CommandResult result = dispatcher.dispatch("player-uuid", "money", List.of(), Map.of());
```

### 5) Endpoints

```java
api.endpoints().register(new EndpointDefinition(
        HttpMethod.GET,
        "/profile",
        "profile.read",
        request -> EndpointResponse.ok("{\"status\":\"ok\"}")
));
```

Puis dispatch:

```java
EndpointDispatcher dispatcher = new EndpointDispatcher(api.endpoints(), api.permissions());
EndpointResponse response = dispatcher.dispatch(
        HttpMethod.GET,
        "/profile",
        new EndpointRequest("player-uuid", Map.of(), Map.of(), null)
);
```

### 6) Stockage

Par defaut, la factory `Aetheris.create(...)` active:
- `MEMORY`
- `FILE` (backend par defaut)

#### MySQL (JDBC)

```java
api.storage().registerProvider(
        StorageBackendType.MYSQL,
        new JdbcStorageProvider("jdbc:mysql://localhost:3306/aetheris", "user", "pass", "aetheris_kv")
);
```

#### Mongo

```java
api.storage().registerProvider(
        StorageBackendType.MONGO,
        new MongoStorageProvider(new MyMongoAdapter())
);
```

#### Ouvrir un namespace

```java
KeyValueStore store = api.storage().open("profiles");
store.put("player-uuid", "{\"level\":12}");
```

## Structure principale

- `fr.aetheris.api`: coeur API, factory, plugin base
- `fr.aetheris.api.service`: registre de services
- `fr.aetheris.api.command`: commandes + dispatcher
- `fr.aetheris.api.event`: event bus
- `fr.aetheris.api.endpoint`: endpoints + dispatcher
- `fr.aetheris.api.security`: permissions + roles
- `fr.aetheris.api.storage`: stockage multi-backends
- `fr.aetheris.api.domain`: interfaces metiers de base (economy, guild, profile)

## Notes

- La couche endpoint est un routeur logique interne (pas un serveur HTTP complet).
- Pour Mongo, tu fournis ton implementation de `MongoAdapter`.
- Pour MySQL, `JdbcStorageProvider` repose sur JDBC standard.

## Licence

A definir selon ton choix (MIT, Apache-2.0, GPL, etc.).
