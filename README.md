# AetherisAPI

AetherisAPI est une librairie Java pour accélérer le développement de plugins Minecraft (Paper/Spigot).

Ce projet n'est pas un plugin autonome : c'est une API/framework que tu inclus dans tes plugins pour gérer rapidement :
- services (Economy, Guild, Profile, etc.)
- GUIs (création/configuration de menus)
- NPCs (téléportation, stats, loot, marchand, dialogue)
- commandes
- events
- endpoints
- permissions et rôles
- stockage (mémoire, fichier, MySQL via JDBC, Mongo via adaptateur)

## Prérequis

- Java 17+
- Maven 3.8+

## Build

```bash
mvn clean package
```

Le JAR est généré dans `target/`.

## Installation dans un autre projet

Ajoute le JAR de cette API comme dépendance dans ton plugin.

Exemple Maven (si publié sur un repo) :

```xml
<dependency>
    <groupId>fr.aetheris.api</groupId>
    <artifactId>aetherisapi</artifactId>
    <version>1.1.0</version>
</dependency>
```

## Utilisation rapide

### 1. Classe principale de ton plugin

```text
import fr.aetheris.api.AetherisApi;
import fr.aetheris.api.AetherisPlugin;

public final class MyPlugin extends AetherisPlugin {
    @Override
    protected void configure(AetherisApi api) {
        // Enregistre services, rôles, endpoints, etc.
    }

    @Override
    protected void onAetherisEnable(AetherisApi api) {
        // Startup
    }
}
```

### 2. Enregistrer un service métier

```text
api.services().register(EconomyService.class, new MyEconomyService());
```

### 2 bis. Services GUI/NPC (déjà actifs)

```text
// De base, Aetheris.create(...) enregistre déjà :
// - PersistentGuiService (namespace storage: "guis")
// - Endpoints CRUD GUI
// - PersistentNpcService (namespace storage: "npcs")
// - Endpoints CRUD NPC
```

Création d'une GUI :

```text
import fr.aetheris.api.domain.gui.*;

GuiDefinition gui = new GuiDefinition(
        "config-main",
        "Configuration",
        GuiType.CHEST,
        3,
        27,
        "gui.use",
        List.of(
                new GuiButton(11, "Stats", "BOOK", GuiActionType.ENDPOINT, "/profile", false),
                new GuiButton(15, "Fermer", "BARRIER", GuiActionType.CLOSE, "", true)
        ),
        Map.of("category", "config")
);

api.guis().save(gui);
```

Puis création d'un NPC :

```text
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

### 2 ter. Brancher les actions NPC sur ton plugin

```text
import fr.aetheris.api.domain.npc.*;

NpcInteractionResult result = api.npcs().interact(
        "npc-teleport-hub",
        new NpcInteractionContext(playerId, NpcInteractionType.INTERACT, Map.of())
);

NpcInteractionExecutor executor = new NpcInteractionExecutor();
executor.execute(playerId, result, new NpcExecutionAdapter() {
    @Override
    public void teleport(String playerId, NpcLocation destination) {
        // Téléporte le joueur avec ton plugin NPC / Bukkit API
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

### 2 quater. Endpoints NPC CRUD

Endpoints enregistrés automatiquement :
- `POST /npcs/create`
- `PUT /npcs/update`
- `GET /npcs/get?id=<npcId>`
- `GET /npcs/list` (option : `type=TELEPORT|STATS|LOOT|MERCHANT|DIALOGUE|CUSTOM`)
- `DELETE /npcs/delete?id=<npcId>`
- `POST /npcs/interact`

Payload `create/update` : query params ou body au format `.properties`.
Exemple body :

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

### 2 quinquies. Endpoints GUI CRUD

Endpoints enregistrés automatiquement :
- `POST /guis/create`
- `PUT /guis/update`
- `GET /guis/get?id=<guiId>`
- `GET /guis/list`
- `DELETE /guis/delete?id=<guiId>`
- `POST /guis/open`

Payload `create/update` : query params ou body au format `.properties`.
Exemple body :

```properties
id=config-main
title=Configuration
type=CHEST
rows=3
size=27
permission=gui.use
metadata=category:config;version:1
buttons=11,Stats,ENDPOINT,/profile,BOOK,false;15,Fermer,CLOSE,,BARRIER,true
```

### 2 sexies. Ouvrir les GUI en jeu (Bukkit/Paper)

```text
import fr.aetheris.api.AetherisApi;
import fr.aetheris.api.AetherisPlugin;
import fr.aetheris.api.domain.gui.bukkit.BukkitGuiBridge;

public final class MyPlugin extends AetherisPlugin {
    private BukkitGuiBridge guiBridge;

    @Override
    protected void onAetherisEnable(AetherisApi api) {
        guiBridge = new BukkitGuiBridge(api, this);
        guiBridge.register();
    }
}
```

Puis ouvrir une GUI :

```text
guiBridge.open(player, "config-main");
```

### 3. Permissions et rôles

```text
api.roles().createRole(new Role("admin", Set.of("eco.manage", "guild.manage"), Set.of()));
api.roles().assignRole("player-uuid", "admin");

boolean canManageEco = api.permissions().hasPermission("player-uuid", "eco.manage");
```

### 4. Commandes

```text
api.commands().register(new AetherisCommand(
        "money",
        "Afficher la balance",
        "eco.read",
        List.of("bal"),
        context -> CommandResult.SUCCESS
));
```

Puis dispatch :

```text
CommandDispatcher dispatcher = new CommandDispatcher(api.commands(), api.permissions());
CommandResult result = dispatcher.dispatch("player-uuid", "money", List.of(), Map.of());
```

### 5. Endpoints

```text
api.endpoints().register(new EndpointDefinition(
        HttpMethod.GET,
        "/profile",
        "profile.read",
        request -> EndpointResponse.ok("{\"status\":\"ok\"}")
));
```

Puis dispatch :

```text
EndpointDispatcher dispatcher = new EndpointDispatcher(api.endpoints(), api.permissions());
EndpointResponse response = dispatcher.dispatch(
        HttpMethod.GET,
        "/profile",
        new EndpointRequest("player-uuid", Map.of(), Map.of(), null)
);
```

### 6. Stockage

Par défaut, la factory `Aetheris.create(...)` active :
- `MEMORY`
- `FILE` (backend par défaut)

#### MySQL (JDBC)

```text
api.storage().registerProvider(
        StorageBackendType.MYSQL,
        new JdbcStorageProvider("jdbc:mysql://localhost:3306/aetheris", "user", "pass", "aetheris_kv")
);
```

#### Mongo

```text
api.storage().registerProvider(
        StorageBackendType.MONGO,
        new MongoStorageProvider(new MyMongoAdapter())
);
```

#### Ouvrir un namespace

```text
KeyValueStore store = api.storage().open("profiles");
store.put("player-uuid", "{\"level\":12}");
```

## Structure principale

- `fr.aetheris.api` : cœur API, factory, plugin base
- `fr.aetheris.api.service` : registre de services
- `fr.aetheris.api.command` : commandes + dispatcher
- `fr.aetheris.api.event` : event bus
- `fr.aetheris.api.endpoint` : endpoints + dispatcher
- `fr.aetheris.api.security` : permissions + rôles
- `fr.aetheris.api.storage` : stockage multi-backends
- `fr.aetheris.api.domain` : interfaces métiers de base (economy, guild, profile)
- `fr.aetheris.api.domain.gui` : création GUI (définition/boutons/actions)
- `fr.aetheris.api.domain.npc` : création NPC (types/actions/interactions)

## Notes

- La couche endpoint est un routeur logique interne (pas un serveur HTTP complet).
- Pour Mongo, tu fournis ton implémentation de `MongoAdapter`.
- Pour MySQL, `JdbcStorageProvider` repose sur JDBC standard.

## Licence

À définir selon ton choix (MIT, Apache-2.0, GPL, etc.).

