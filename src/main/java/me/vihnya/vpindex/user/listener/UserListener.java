package me.vihnya.vpindex.user.listener;

import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent;
import io.papermc.paper.event.server.WhitelistStateUpdateEvent;

import lombok.RequiredArgsConstructor;

import me.vihnya.vpindex.VPIndex;
import me.vihnya.vpindex.user.User;
import me.vihnya.vpindex.user.service.UserService;
import me.vihnya.vpindex.user.session.UserSession;
import me.vihnya.vpindex.util.AfkUtil;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.Arrays;

@RequiredArgsConstructor
public class UserListener implements Listener {

    private final UserService userService;

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        String name = e.getPlayer().getName();
        User user = this.userService.getUser(name);
        if (user == null) {
            if (VPIndex.getPlugin(VPIndex.class).getConfig().getBoolean("settings.white-list")) {
                if (e.getPlayer().isWhitelisted()) {
                    user = new User(name);
                    this.userService.createUser(user);
                }
            }
            else {
                user = new User(name);
                this.userService.createUser(user);
            }
        }
        if (user != null) {
            user.setOnline(true);
            UserSession userSession = new UserSession(System.currentTimeMillis());
            user.setActiveSession(userSession);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (userService.getUser(player.getName()) != null) {
            User user = userService.getUser(player.getName());
            if (user == null) {
                throw new RuntimeException("Сессия игрока не была сохранена! (Вероятно, перед этим сервер перезагружали через /reload)");
            }
            user.setOnline(false);

            userService.saveUserSession(user);

            userService.saveUser(user);

            AfkUtil.clearCache(player.getName());
        }
    }

    @EventHandler
    public void onWhitelistCommand(PlayerCommandPreprocessEvent e) {
        if (VPIndex.getPlugin(VPIndex.class).getConfig().getBoolean("settings.white-list")) {
            String name;
            if (e.getMessage().contains("/whitelist add ")) {
                name = e.getMessage().replace("/whitelist add ", "");

                User user = new User(name);
                this.userService.createUser(user);
                if (Bukkit.getPlayer(name) != null) {
                    if (Bukkit.getPlayer(name).isOnline()) {
                        user.setOnline(true);
                        UserSession userSession = new UserSession(System.currentTimeMillis());
                        user.setActiveSession(userSession);
                    }
                }
            }

            if (e.getMessage().contains("/whitelist remove ")) {
                name = e.getMessage().replace("/whitelist remove ", "");
                if (this.userService.getUser(name) != null) {
                    User user = this.userService.getUser(name);
                    if (user.getSessions() != null) {
                        this.userService.deleteSessions(user);
                    }
                    this.userService.deleteUser(user);
                }
            }
        }
    }
}
