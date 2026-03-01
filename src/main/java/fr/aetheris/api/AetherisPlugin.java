package fr.aetheris.api;

import java.nio.file.Path;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class AetherisPlugin extends JavaPlugin {

    private AetherisApi api;

    @Override
    public final void onEnable() {
        final Path dataDirectory = getDataFolder().toPath();
        api = Aetheris.create(dataDirectory);
        configure(api);
        onAetherisEnable(api);
    }

    @Override
    public final void onDisable() {
        try {
            onAetherisDisable(api);
        } finally {
            if (api != null) {
                api.close();
            }
            api = null;
        }
    }

    public final AetherisApi api() {
        if (api == null) {
            throw new IllegalStateException("API is not available outside plugin lifecycle.");
        }
        return api;
    }

    protected void configure(AetherisApi api) {
    }

    protected abstract void onAetherisEnable(AetherisApi api);

    protected void onAetherisDisable(AetherisApi api) {
    }
}
