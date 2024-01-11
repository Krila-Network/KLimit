package awa.catnosoul.klimit;

import gnu.trove.TCollections;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

public class KLimit extends Plugin implements Listener {
    private final TObjectIntMap<InetAddress> addresses = TCollections.synchronizedMap(new TObjectIntHashMap());
    private int limit = 3;

    public KLimit() {
    }

    public void onEnable() {
        File confFile = new File(this.getDataFolder(), "krila.yml");
        confFile.getParentFile().mkdirs();
        ConfigurationProvider yaml = ConfigurationProvider.getProvider(YamlConfiguration.class);

        try {
            confFile.createNewFile();
            Configuration conf = yaml.load(confFile);
            if (conf.getInt("limit") == 0) {
                conf.set("limit", 1);
            }

            this.limit = conf.getInt("limit");
            yaml.save(conf, confFile);
        } catch (IOException var4) {
            this.getLogger().log(Level.WARNING, "&f[&cKrila-IP&7]&cError loading configuration.", var4);
        }

        this.getProxy().getPluginManager().registerListener(this, this);
    }

    @EventHandler
    public void login(LoginEvent event) {
        if (this.addresses.get(event.getConnection().getAddress().getAddress()) >= this.limit) {
            event.setCancelReason("&7[&cKrila-IP&7]&cReached maximum number of connections.");
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void postLogin(PostLoginEvent event) {
        this.addresses.adjustOrPutValue(event.getPlayer().getAddress().getAddress(), 1, 1);
    }

    @EventHandler
    public void disconnect(PlayerDisconnectEvent event) {
        InetAddress addr = event.getPlayer().getAddress().getAddress();
        this.addresses.adjustValue(addr, -1);
        if (this.addresses.get(addr) <= 0) {
            this.addresses.remove(addr);
        }

    }
}
