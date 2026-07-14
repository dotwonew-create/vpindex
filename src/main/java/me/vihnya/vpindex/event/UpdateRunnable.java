package me.vihnya.vpindex.event;

import lombok.RequiredArgsConstructor;

import me.vihnya.vpindex.config.Config;
import me.vihnya.vpindex.user.User;
import me.vihnya.vpindex.user.service.UserService;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class UpdateRunnable extends BukkitRunnable {

    private final UserService userService;

    private final Config config;
    private final Map<String, String> userMap = new HashMap<>();

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            User user = userService.getUser(player.getName());
            if (user == null)
                continue;
            userMap.put(player.getName(), user.getIndexAsString(config.getTotalTime(), 0));
        }
    }

    public String getUserIndex(User user) {
        String value = userMap.get(user.getName());
        if (value == null) {
            userMap.put(user.getName(), user.getIndexAsString(config.getTotalTime(), 0));
            return user.getIndexAsString(config.getTotalTime(), 0);
        }
        return value;
    }
}
