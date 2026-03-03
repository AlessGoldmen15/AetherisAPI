# AetherisAPI

AetherisAPI est une librairie Java pour accélérer le développement de plugins Minecraft (Paper/Spigot).

Ce projet n'est pas un plugin autonome: c'est une API/framework que tu inclus dans tes plugins pour gérer rapidement:
- services (Economy, Guild, Profile, etc.)
- NPCs (teleportation, stats, loot, marchand, dialogue)
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

### 2 bis) Service NPC (deja actif)

```java
// De base, Aetheris.create(...) enregistre deja:
// - PersistentNpcService (namespace storage: "npcs")
// - Endpoints CRUD NPC
```

Puis creation d'un NPC:

```java
import fr.aetheris.api.domain.npc.*;

NpcDefinition npc = new NpcDefinition(
        "npc-teleport-hub",
        "Voyageur",
        NpcType.TELEPORT,
        new NpcLocation("world", 100, 65, 100, 0f, 0f),
        Map.of("skin", "traveler"),
        List.of(new TeleportAction(new NpcLocation("spawn", 0, 64, 0, 90f, 0f)))
);

api.npcs().save(npc);
```
### 2 ter) Brancher les actions NPC sur ton plugin

```java
import fr.aetheris.api.domain.npc.*;

NpcInteractionResult result = api.npcs().interact(
        "npc-teleport-hub",
        new NpcInteractionContext(playerId, NpcInteractionType.INTERACT, Map.of())
);

NpcInteractionExecutor executor = new NpcInteractionExecutor();
executor.execute(playerId, result, new NpcExecutionAdapter() {
    @Override
    public void teleport(String playerId, NpcLocation destination) {
        // Teleporte le joueur avec ton plugin NPC / Bukkit API
    }

    @Override
    public void applyStat(String playerId, String statKey, double value, StatOperation operation) {
        // Applique les stats
    }

    @Override
    public void grantLoot(String playerId, NpcLootEntry entry, int amount) {
        // Donne le loot
    }

    @Override
    public void openMerchant(String playerId, List<NpcMerchantOffer> offers) {
        // Ouvre le shop
    }

    @Override
    public void startDialogue(String playerId, String dialogueId, List<String> lines) {
        // Lance le dialogue
    }

    @Override
    public void handleCustomAction(String playerId, NpcAction action) {
        // Gestion custom
    }
});
```

### 2 quater) Endpoints NPC CRUD

Endpoints enregistres automatiquement:
- `POST /npcs/create`
- `PUT /npcs/update`
- `GET /npcs/get?id=<npcId>`
- `GET /npcs/list` (option: `type=TELEPORT|STATS|LOOT|MERCHANT|DIALOGUE|CUSTOM`)
- `DELETE /npcs/delete?id=<npcId>`
- `POST /npcs/interact`

Payload `create/update` accepte query params et body au format `.properties`.
Exemple body:

```properties
id=npc-shop-1
name=Marchand Central
type=MERCHANT
world=world
x=120
y=64
z=88
yaw=180
pitch=0
attributes=skin:villager;faction:city
merchant=diamond,1,250,coins;gold_ingot,5,80,coins
dialogueId=shop_welcome
dialogue=Bienvenue|Tu cherches quelque chose ?
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
- `fr.aetheris.api.domain.npc`: creation NPC (types/actions/interactions)

## Notes

- La couche endpoint est un routeur logique interne (pas un serveur HTTP complet).
- Pour Mongo, tu fournis ton implementation de `MongoAdapter`.
- Pour MySQL, `JdbcStorageProvider` repose sur JDBC standard.

## Licence

A definir selon ton choix (MIT, Apache-2.0, GPL, etc.).


