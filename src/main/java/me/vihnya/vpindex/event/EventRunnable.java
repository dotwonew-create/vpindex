package me.vihnya.vpindex.event;

import lombok.RequiredArgsConstructor;

import me.vihnya.vpindex.user.User;
import me.vihnya.vpindex.user.service.UserService;
import me.vihnya.vpindex.util.Pair;
import me.vihnya.vpindex.util.PlayerCooldownUtil;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

@RequiredArgsConstructor
public class EventRunnable extends BukkitRunnable {

    private final UserService userService;

    @Override
    public void run() {
        for (User user : userService.cachedUsers()) {
            Player player = Bukkit.getPlayer(user.getName());
            if (player == null) {
                continue;
            }
            for (Map.Entry<String, Long> entry : user.getEventMap().entrySet()) {
                String key = entry.getKey();
                String[] parts = key.split("\\|");
                EventType eventType = EventType.valueOf(parts[0]);
                String data = parts[1];
                if (PlayerCooldownUtil.hasCooldown(key, player)) {
                    Pair<Long, Boolean> pair = user.getLastPoints(eventType, data);
                    if (pair == null) {
                        continue;
                    }
                    if (pair.getLeft() == -1 && pair.getRight()) {
                        continue;
                    }
                    if (pair.getLeft() != -1 && user.getSeconds(eventType, data) >= pair.getLeft()) {
                        continue;
                    }
                    user.addSeconds(eventType, data, 1);
                } else {
                    if (user.getSeconds(eventType, data) > 0) {
                        user.setSeconds(eventType, data, user.getSeconds(eventType, data) - 1);
                    }
                    if (user.getSeconds(eventType, data) == 0) {
                        PlayerCooldownUtil.removeCooldown(key, player);
                    }
                }
            }

        }
    }
}
