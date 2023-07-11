package me.petterim1.bossbartext;

import cn.nukkit.event.EventPriority;
import cn.nukkit.event.player.PlayerLocallyInitializedEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.utils.BossBarColor;
import cn.nukkit.utils.DummyBossBar;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main extends PluginBase implements Listener {

    private Map<String, String> text;
    private int length;
    private BossBarColor color;
    private static final Map<Long, Long> bossBars = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        this.text = (Map<String, String>) this.getConfig().get("text");
        this.length = this.getConfig().getInt("length", 100);
        try {
            this.color = BossBarColor.valueOf(this.getConfig().getString("color"));
        } catch (IllegalArgumentException ignore) {
        }
    }

    @EventHandler
    public void onJoin(PlayerLocallyInitializedEvent e) {
        DummyBossBar.Builder builder = new DummyBossBar.Builder(e.getPlayer());
        if (color != null) {
            builder.color(color);
        }

        bossBars.put(
                e.getPlayer().getId(),
                e.getPlayer().createBossBar(builder.text(text.getOrDefault(e.getPlayer().getLevel().getName(), "")).length((float) length).build())
        );
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        bossBars.remove(e.getPlayer().getId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onLevelChange(PlayerTeleportEvent e) {
        if (e.getTo().getLevel() != e.getFrom().getLevel()) {
            Long id = bossBars.get(e.getPlayer().getId());
            if (id == null) {
                return;
            }

            DummyBossBar bb = e.getPlayer().getDummyBossBar(id);
            if (bb == null) {
                return;
            }

            bb.setText(text.getOrDefault(e.getTo().getLevel().getName(), ""));
        }
    }
}
