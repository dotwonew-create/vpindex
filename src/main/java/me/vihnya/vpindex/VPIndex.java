package me.vihnya.vpindex;

import com.j256.ormlite.dao.CloseableWrappedIterable;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.table.TableUtils;

import lombok.Getter;

import me.vihnya.vpindex.afk.AfkRunnable;
import me.vihnya.vpindex.afk.AfkServiceImpl;
import me.vihnya.vpindex.command.IndexCommand;
import me.vihnya.vpindex.config.Config;
import me.vihnya.vpindex.config.HologramsConfig;
import me.vihnya.vpindex.config.MenusConfig;
import me.vihnya.vpindex.config.MessageConfig;
import me.vihnya.vpindex.event.EventListener;
import me.vihnya.vpindex.event.EventRunnable;
import me.vihnya.vpindex.event.MenuListener;
import me.vihnya.vpindex.event.UpdateRunnable;
import me.vihnya.vpindex.holograms.Hologram;
import me.vihnya.vpindex.holograms.HologramImpl;
import me.vihnya.vpindex.logger.FileLogger;
import me.vihnya.vpindex.menu.api.PlayerMenuUtility;
import me.vihnya.vpindex.placeholder.Placeholders;
import me.vihnya.vpindex.user.User;
import me.vihnya.vpindex.user.listener.UserListener;
import me.vihnya.vpindex.user.service.UserService;
import me.vihnya.vpindex.user.service.UserServiceImpl;
import me.vihnya.vpindex.user.session.UserSession;
import me.vihnya.vpindex.util.AfkUtil;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class VPIndex extends JavaPlugin {

    @Getter
    private static Hologram hologramManager = new HologramImpl();
    private static UserService userService;
    private static final HashMap<Player, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();
    private FileLogger fileLogger;

    @Override
    public void onEnable() {

        fileLogger = new FileLogger(this);
        saveDefaultConfig();

        Config config = new Config(this);
        config.loadValues();

        MessageConfig.createConfig();
        MessageConfig.get().options().copyDefaults(true);
        MessageConfig.save();

        MenusConfig.createConfig();
        MenusConfig.get().options().copyDefaults(true);
        MenusConfig.save();

        HologramsConfig.setup();
        HologramsConfig.get().addDefault("basicTags", List.of("moder", "VPText"));
        HologramsConfig.get().options().copyDefaults(true);
        HologramsConfig.save();

        try {
            setup(config);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        EventRunnable eventRunnable = new EventRunnable(userService);
        eventRunnable.runTaskTimer(this, 0L, 20L);

        UpdateRunnable updateRunnable = new UpdateRunnable(userService, config);
        updateRunnable.runTaskTimer(this, 0L, 20 * config.getUpdateTime());

        AfkServiceImpl afkService = new AfkServiceImpl(userService, config);
        AfkUtil.init(afkService);

        AfkRunnable afkRunnable = new AfkRunnable(afkService);
        afkRunnable.runTaskTimer(this, 0L, 20L);

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(afkService, this);

        if (pluginManager.getPlugin("PlaceholderAPI") != null && pluginManager.isPluginEnabled("PlaceholderAPI")) {
            new Placeholders(userService, config, updateRunnable).register();
            getLogger().info("PlaceholderAPI обнаружен, поддержка включена");
        }

        IndexCommand indexCommand = new IndexCommand(config, userService, fileLogger);

        getCommand("index").setExecutor(indexCommand);
        getCommand("index").setTabCompleter(indexCommand);

        getServer().getPluginManager().registerEvents(new MenuListener(), this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            User user = userService.getUser(player.getName());
            if (user == null) {
                continue;
            }
            user.setOnline(true);

            UserSession userSession = new UserSession(System.currentTimeMillis());
            user.setActiveSession(userSession);
        }
    }

    @Override
    public void onDisable() {
        fileLogger.close();

        for (User user : userService.cachedUsers()) {
            if (user.getActiveSession() != null) {
                userService.saveUserSession(user);
            }
            userService.saveUser(user);
        }
    }

    private void setup(Config config) throws SQLException {
        try {

            JdbcConnectionSource connectionSource = new JdbcConnectionSource("jdbc:sqlite://" + getDataFolder().getAbsolutePath() + "/database.db");

            TableUtils.createTableIfNotExists(connectionSource, User.class);
            TableUtils.createTableIfNotExists(connectionSource, UserSession.class);

            Dao<User, Long> userDao = DaoManager.createDao(connectionSource, User.class);
            Dao<UserSession, Long> userSessionDao = DaoManager.createDao(connectionSource, UserSession.class);

            userService = new UserServiceImpl(userDao, userSessionDao);

            Bukkit.getPluginManager().registerEvents(new EventListener(userService, config), this);
            Bukkit.getPluginManager().registerEvents(new UserListener(userService), this);

            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                List<UserSession> toDelete = new ArrayList<>();
                try (CloseableWrappedIterable<UserSession> wrappedIterable
                             = userSessionDao.getWrappedIterable()) {
                    wrappedIterable.forEach(session -> {
                        long passed = (System.currentTimeMillis() - session.getTime()) / 1000;
                        if (passed > config.getClearTime()) {
                            toDelete.add(session);
                            if (session.getUser() != null) {
                                fileLogger.log("Удаление сессии игрока " + session.getUser().getName() + ", data: " + session.getPlayed() + "|" + session.getStartPlay() + "|" + session.getPoints() + "|" + session.getTime(), "CONSOLE");
                            }
                        }
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                for (UserSession session : toDelete) {
                    try {
                        userSessionDao.delete(session);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (RuntimeException e) {
            return;
        }
    }

    public static PlayerMenuUtility getPlayerMenuUtility(Player p) {
        PlayerMenuUtility playerMenuUtility;
        if (!(playerMenuUtilityMap.containsKey(p))) {

            playerMenuUtility = new PlayerMenuUtility(p);
            playerMenuUtilityMap.put(p, playerMenuUtility);

            return playerMenuUtility;
        } else {
            return playerMenuUtilityMap.get(p);
        }
    }

    public static UserService getService() {
        return userService;
    }

}
