package me.vihnya.vpindex.afk;

import lombok.RequiredArgsConstructor;

import me.vihnya.vpindex.afk.event.PlayerAfkEvent;
import me.vihnya.vpindex.afk.event.PlayerNotAfkEvent;
import me.vihnya.vpindex.config.Config;
import me.vihnya.vpindex.user.User;
import me.vihnya.vpindex.user.service.UserService;
import me.vihnya.vpindex.util.TimeUtil;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class AfkServiceImpl implements AfkService, Listener {

    private final UserService userService;

    private final Config config;
    private final Map<String, Long> lastMoveMap = new HashMap<>();

    private final Map<String, Boolean> afkMap = new HashMap<>();

    private final Map<String, Long> afkTimeMap = new HashMap<>();

    private final Map<String, Long> afkStartTimeMap = new HashMap<>();
    @Override
    public long getAfkTime(String name) {
        long time = afkTimeMap.getOrDefault(name, 0L);
        if (isAfk(name)) {
            time += (System.currentTimeMillis() - afkStartTimeMap.getOrDefault(name, System.currentTimeMillis())) / 1000;
        }
        return time;
    }

    @Override
    public long getLastMoveTime(String name) {
        return lastMoveMap.getOrDefault(name, 0L);
    }

    @Override
    public long getAfkThreshold() {
        return config.getAfkTime();
    }

    @Override
    public boolean isAfk(String name) {
        return afkMap.getOrDefault(name, false);
    }

    @Override
    public void setAfk(String name, boolean afk) {
        afkMap.put(name, afk);
    }

    @Override
    public void clearCache(String name) {
        afkTimeMap.remove(name);
        afkMap.remove(name);
        afkStartTimeMap.remove(name);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        lastMoveMap.put(p.getName(), System.currentTimeMillis());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        lastMoveMap.put(p.getName(), System.currentTimeMillis());
    }

    @EventHandler
    public void onAfk(PlayerAfkEvent event) {
        User user = userService.getUser(event.getPlayer().getName());

        afkStartTimeMap.put(event.getPlayer().getName(), System.currentTimeMillis());

        if (user != null) {
            user.sendDebugMessage(event.getPlayer(), config.isDebugMode(), " ");
            user.sendDebugMessage(event.getPlayer(), config.isDebugMode(), "&7[DEBUG] Вы вошли в режим AFK");
            user.sendDebugMessage(event.getPlayer(), config.isDebugMode(), " ");
        }
    }

    @EventHandler
    public void onAfkNot(PlayerNotAfkEvent event) {
        User user = userService.getUser(event.getPlayer().getName());
        String name = event.getPlayer().getName();

        long afkStartTime =  afkStartTimeMap.get(event.getPlayer().getName());
        long afkTime = (System.currentTimeMillis() - afkStartTime) / 1000;
        afkTimeMap.put(name, afkTimeMap.getOrDefault(name, 0L) + afkTime);

        if (user != null) {
            user.sendDebugMessage(event.getPlayer(), config.isDebugMode(), " ");
            user.sendDebugMessage(event.getPlayer(), config.isDebugMode(), "&7[DEBUG] Вы вышли из режима AFK, в нем вы пробыли: " + TimeUtil.getTime(afkTime));
            user.sendDebugMessage(event.getPlayer(), config.isDebugMode(), "&7[DEBUG] Общее время в афк: " + TimeUtil.getTime(getAfkTime(name)));
            user.sendDebugMessage(event.getPlayer(), config.isDebugMode(), " ");
        }
    }
}
