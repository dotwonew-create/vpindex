package me.vihnya.vpindex.afk;

import lombok.RequiredArgsConstructor;

import me.vihnya.vpindex.afk.event.PlayerAfkEvent;
import me.vihnya.vpindex.afk.event.PlayerNotAfkEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class AfkRunnable extends BukkitRunnable {

    private final AfkService afkService;

    @Override
    public void run() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            long lastMove = afkService.getLastMoveTime(player.getName());
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastMove >= afkService.getAfkThreshold()) {
                if (!afkService.isAfk(player.getName())) {
                    afkService.setAfk(player.getName(), true);
                    Bukkit.getServer().getPluginManager().callEvent(new PlayerAfkEvent(player));
                }
            } else {
                if (afkService.isAfk(player.getName())) {
                    afkService.setAfk(player.getName(), false);
                    Bukkit.getServer().getPluginManager().callEvent(new PlayerNotAfkEvent(player));
                }
            }
        }
    }
}
