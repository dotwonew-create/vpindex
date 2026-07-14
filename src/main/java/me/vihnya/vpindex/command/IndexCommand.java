package me.vihnya.vpindex.command;

import lombok.RequiredArgsConstructor;

import me.clip.placeholderapi.PlaceholderAPI;

import me.vihnya.vpindex.VPIndex;
import me.vihnya.vpindex.config.Config;
import me.vihnya.vpindex.config.HologramsConfig;
import me.vihnya.vpindex.config.MenusConfig;
import me.vihnya.vpindex.config.MessageConfig;
import me.vihnya.vpindex.logger.FileLogger;
import me.vihnya.vpindex.menu.SortMenu;
import me.vihnya.vpindex.user.User;
import me.vihnya.vpindex.user.service.UserService;
import me.vihnya.vpindex.user.session.UserSession;
import me.vihnya.vpindex.util.AfkUtil;
import me.vihnya.vpindex.util.NumberUtil;
import me.vihnya.vpindex.util.StringUtil;
import me.vihnya.vpindex.util.TimeUtil;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class IndexCommand implements CommandExecutor, TabExecutor {

    private final Config config;

    private final UserService userService;

    private final FileLogger fileLogger;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!commandSender.hasPermission("vpindex.admin")) {
            commandSender.sendMessage(config.getMessage("noPermission"));
            return true;
        }

        Player playerSender = (Player) commandSender;

        if (args.length < 1) {
            for (String line : MessageConfig.get().getStringList("help")) {
                commandSender.sendMessage(PlaceholderAPI.setPlaceholders((Player) commandSender, StringUtil.colorize(line)));
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            try {
                config.reload();
                MessageConfig.reload();
                HologramsConfig.reload();
                MenusConfig.reload();
                VPIndex.getHologramManager().reload();
                commandSender.sendMessage(config.getMessage("configReloaded"));
                fileLogger.log("/index reload", commandSender.getName());
                return true;
            } catch (Exception e) {
                commandSender.sendMessage(config.getMessage("reloadHelp"));
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("sort")) {

            try {

                int number;

                if (args[2].equalsIgnoreCase("#all")) {
                    number = userService.users().size();
                } else {
                    number = Integer.parseInt(args[2]);
                }
                new SortMenu(VPIndex.getPlayerMenuUtility((Player) commandSender), args[1], number, args[3]).open();
                fileLogger.log("/index sort " + args[1] + " " + args[2] + " " + args[3], commandSender.getName());
                return true;
            } catch (Exception e) {
                commandSender.sendMessage(config.getMessage("sortHelp"));
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("debug")) {
            try {
                if (args[1].equalsIgnoreCase("#all")) {
                    boolean debug = !config.isDebugMode();
                    config.setDebugMode(debug);
                    String message = debug ? config.getMessage("debugModeOn") : config.getMessage("debugModeOff");
                    commandSender.sendMessage(message.replace("%player%", "ВСЕ"));
                    fileLogger.log("/index debug " + args[1], commandSender.getName());
                    return true;
                }
                User user = userService.getUser(args[1]);
                if (user == null) {
                    commandSender.sendMessage(config.getMessage("playerNotFound"));
                    return true;
                }
                boolean debug = !user.isDebugMode();
                user.setDebugMode(debug);

                String message = debug ? config.getMessage("debugModeOn") : config.getMessage("debugModeOff");
                commandSender.sendMessage(message.replace("%player%", args[1]));
                fileLogger.log("/index debug " + args[1], commandSender.getName());
                return true;
            } catch (Exception e) {
                commandSender.sendMessage(config.getMessage("debugHelp"));
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("addpoint")) {
            try {

                double points;
                try {
                    points = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    commandSender.sendMessage(config.getMessage("enterNumber"));
                    return true;
                }
                if (args[1].equalsIgnoreCase("#all")) {
                    String message = config.getMessage("addpointAll");
                    for (User user : userService.users()) {
                        addPoints(user, points);
                    }
                    commandSender.sendMessage(message.replace("%points%", points + ""));
                    fileLogger.log("/index addpoint " + args[1] + " " + points, commandSender.getName());
                    return true;
                }
                if (args[1].equalsIgnoreCase("#online")) {
                    String message = config.getMessage("addpointAll");
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        User user = userService.getUser(player.getName());
                        if (user == null) {
                            continue;
                        }
                        addPoints(user, points);
                    }
                    commandSender.sendMessage(message.replace("%points%", points + ""));
                    fileLogger.log("/index addpoint " + args[1] + " " + points, commandSender.getName());
                    return true;
                }
                User user = userService.getUser(args[1]);
                if (user == null) {
                    commandSender.sendMessage(config.getMessage("playerNotFound"));
                    return true;
                }
                addPoints(user, points);
                String message = config.getMessage("addpointPlayer");
                commandSender.sendMessage(message.replace("%player%", args[1]).replace("%points%", points + ""));

                fileLogger.log("/index addpoint " + args[1] + " " + points, commandSender.getName());
                return true;
            } catch (Exception e) {
                commandSender.sendMessage(config.getMessage("addpointHelp"));
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("player")) {
            try {
                String playerName = args[1];
                int period = args.length > 2 ? Integer.parseInt(args[2]) : config.getTotalTime();
                int offset = args.length > 3 ? Integer.parseInt(args[3]) : 0;
                processPlayerCommand(commandSender, playerName, period, offset);
                return true;
            } catch (Exception e) {
                commandSender.sendMessage(config.getMessage("playerHelp"));
                return true;
            }
        }

        if (args[0].equalsIgnoreCase("hologram")) {
                if (args[1].equalsIgnoreCase("create")) {
                    try {
                        VPIndex.getHologramManager().addToConfig(args[2], args[3]);
                        VPIndex.getHologramManager().createHologram(args[2], playerSender.getLocation().getX(), playerSender.getLocation().getY(), playerSender.getLocation().getZ(), playerSender.getLocation().getWorld());
                        commandSender.sendMessage(config.getMessage("hologramCreated").replace("%hologram%", args[2]));
                        fileLogger.log("/index hologram create " + args[2] + args[3], commandSender.getName());
                        return true;
                    } catch (Exception e) {
                        commandSender.sendMessage(config.getMessage("hologramCreateHelp"));
                        return true;
                    }
                }

            if (args[1].equalsIgnoreCase("remove")) {
                    try {
                        VPIndex.getHologramManager().removeHologram(args[2], (Player) commandSender);
                        HologramsConfig.get().set("holograms." + args[2], null);
                        HologramsConfig.save();
                        fileLogger.log("/index hologram delete " + args[2], commandSender.getName());
                        return true;
                    } catch (Exception e) {
                        commandSender.sendMessage(config.getMessage("hologramRemoveHelp"));
                        return true;
                    }
            }

            if (args[1].equalsIgnoreCase("kill")) {
                try {
                    VPIndex.getHologramManager().removeHologram(args[2], (Player) commandSender);
                    fileLogger.log("/index hologram delete " + args[2], commandSender.getName());
                    return true;
                } catch (Exception e) {
                    commandSender.sendMessage(config.getMessage("hologramRemoveHelp"));
                    return true;
                }
            }

            if (args[1].equalsIgnoreCase("restore")) {
                try {
                    VPIndex.getHologramManager().createHologram(args[2], playerSender.getLocation().getX(), playerSender.getLocation().getY(), playerSender.getLocation().getZ(), playerSender.getLocation().getWorld());
                    commandSender.sendMessage(config.getMessage("hologramRestored").replace("%hologram%", args[2]));
                    fileLogger.log("/index hologram restore " + args[2], commandSender.getName());
                    return true;
                } catch (Exception e) {
                    commandSender.sendMessage(config.getMessage("hologramRestoreHelp"));
                    return true;
                }
            }
        }

        if (args[0].equalsIgnoreCase("top")) {
            try {
                int count;
                try {
                    if (args[3].equalsIgnoreCase("#all")) {
                        count = userService.users().size();
                    } else {
                        count = Integer.parseInt(args[3]);
                    }
                } catch (NumberFormatException e) {
                    commandSender.sendMessage(config.getMessage("enterNumber"));
                    return true;
                }
                String type = args[2];
                Bukkit.getScheduler().runTaskAsynchronously(config.getPlugin(), () ->
                {
                    processTopCommand(commandSender, count, type, args[1]);
                });
                fileLogger.log("/index top " + args[1] + " " + type + " " + count, commandSender.getName());
                return true;
            } catch (Exception e) {
                commandSender.sendMessage(config.getMessage("topHelp"));
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("delete")) {
            try {
                String target = args[1];
                int daysAgo;
                int durationDays;
                try {
                    durationDays = args.length > 2 ? Integer.parseInt(args[2]) : -1;
                    daysAgo = args.length > 3 ? Integer.parseInt(args[3]) : -1;
                } catch (NumberFormatException e) {
                    commandSender.sendMessage(config.getMessage("enterNumber"));
                    return true;
                }

                if (target.equalsIgnoreCase("#all")) {
                    deleteAllStats(daysAgo, durationDays, false, Boolean.parseBoolean(args[4]));
                } else if (target.equalsIgnoreCase("#online")) {
                    deleteAllStats(daysAgo, durationDays, true, Boolean.parseBoolean(args[4]));
                } else {
                    User user = userService.getUser(target);

                    if (user != null) {
                        deleteStats(user, daysAgo, durationDays, Boolean.parseBoolean(args[4]));
                    } else {
                        commandSender.sendMessage(config.getMessage("playerNotFound"));
                        return true;
                    }
                }

                commandSender.sendMessage(config.getMessage("statsDeleted"));
                fileLogger.log("/index delete " + target + " " + durationDays + " " + daysAgo + " " + args[4], commandSender.getName());
            } catch (Exception e) {
                commandSender.sendMessage(config.getMessage("deleteHelp"));
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("average")) {

            int durationDays;
            try {
                durationDays = args.length == 3 ? Integer.parseInt(args[2]) : config.getTotalTime();
            }catch (NumberFormatException e) {
                commandSender.sendMessage(config.getMessage("enterNumber"));
                return true;
            }

            Bukkit.getScheduler().runTaskAsynchronously(config.getPlugin(), () -> {
                List<User> users = userService.users();
                double value = 0;

                String path = "averageFormat";

                if (args[1].equalsIgnoreCase("index")) { path = "average.indexFormat"; }
                if (args[1].equalsIgnoreCase("points")) { path = "average.pointsFormat"; }
                if (args[1].equalsIgnoreCase("time")) { path = "average.timeFormat"; }
                if (args[1].equalsIgnoreCase("activeTime")) { path = "average.activeTimeFormat"; }
                if (args[1].equalsIgnoreCase("afkTime")) { path = "average.afkTimeFormat"; }

                for (User user : users) {
                    if (user != null) {

                        if (args[1].equalsIgnoreCase("index")) { value += user.getIndex(durationDays, 0); }
                        if (args[1].equalsIgnoreCase("points")) { value += user.getPoints(durationDays, 0); }
                        if (args[1].equalsIgnoreCase("afkTime")) { value += user.getAfkTime(durationDays, 0); }
                        if (args[1].equalsIgnoreCase("activeTime")) { value += user.getPlayedTime(durationDays, 0); }
                        if (args[1].equalsIgnoreCase("time")) { value += user.getPlayedTime(durationDays, 0) + user.getAfkTime(durationDays, 0); }

                    }
                }
                double average = value / users.size();

                if (args[1].equalsIgnoreCase("time") || args[1].equalsIgnoreCase("afkTime") || args[1].equalsIgnoreCase("activeTime")) {
                    String averageTime = TimeUtil.getTime((long) (value / users.size()));
                    commandSender.sendMessage(config.getMessage(path).replace("%average%", averageTime));
                } else {
                    commandSender.sendMessage(config.getMessage(path).replace("%average%", NumberUtil.round(average, 6)));
                }
                fileLogger.log("/index average " + args[1] + " " + durationDays, commandSender.getName());
            });

        }
        return false;
    }
    private void processTopCommand(CommandSender sender, int count, String type, String result) {
        List<User> users = userService.users();

        String header = PlaceholderAPI.setPlaceholders((Player) sender, StringUtil.colorize(MessageConfig.get().getString("top." + result + ".header")));
        String footer = PlaceholderAPI.setPlaceholders((Player) sender, StringUtil.colorize(MessageConfig.get().getString("top." + result + ".footer")));;

        if (result.equalsIgnoreCase("index")) { users.sort(Comparator.comparingDouble(user -> user.getIndex(config.getTotalTime(), 0))); }
        if (result.equalsIgnoreCase("activeTime")) { users.sort(Comparator.comparingDouble(user -> user.getPlayedTime(config.getTotalTime(), 0) - user.getAfkTime(config.getTotalTime(), 0))); }
        if (result.equalsIgnoreCase("points")) { users.sort(Comparator.comparingDouble(user -> user.getPoints(config.getTotalTime(), 0))); }
        if (result.equalsIgnoreCase("time")) { users.sort(Comparator.comparingDouble(user -> user.getAfkTime(config.getTotalTime(), 0) + user.getPlayedTime(config.getTotalTime(), 0))); }
        if (result.equalsIgnoreCase("afkTime")) { users.sort(Comparator.comparingDouble(user -> user.getAfkTime(config.getTotalTime(), 0))); }

        Collections.reverse(users);

        List<User> topUsers;

        if (type.equalsIgnoreCase("best")) {
            topUsers = users.stream().limit(count).collect(Collectors.toList());
        } else if (type.equalsIgnoreCase("worst")) {
            topUsers = users.stream().skip(Math.max(0, users.size() - count)).collect(Collectors.toList());
        } else {
            return;
        }

        List<String> topMessage = new ArrayList<>(topUsers.size());

        if (count != users.size()) {
            topMessage.add(header.replace("%type_uppercase%", type.toUpperCase()).replace("%type_lowercase%", type.toLowerCase()).replace("%count%", String.valueOf(count)));
        } else {
            topMessage.add(header.replace("%type_uppercase%", type.toUpperCase()).replace("%type_lowercase%", type.toLowerCase()).replace("%count%", "ВСЕ"));
        }

        for (int i = 0; i < topUsers.size(); i++) {
            topMessage.add("NULL");
        }

        int position;
        for (int i = 0; i < topUsers.size(); i++) {
            position = type.equalsIgnoreCase("best") ? i + 1 : topUsers.size() - i;
            User user = topUsers.get(i);

            String value = "NONE";

            if (result.equalsIgnoreCase("index")) { value = user.getIndexAsString(config.getTotalTime(), 0); }
            if (result.equalsIgnoreCase("points")) { value = user.getPointsAsString(config.getTotalTime(), 0); }
            if (result.equalsIgnoreCase("activeTime")) { value = TimeUtil.getTime(user.getAfkTime(config.getTotalTime(), 0) + user.getPlayedTime(config.getTotalTime(), 0)); }
            if (result.equalsIgnoreCase("time")) { value = TimeUtil.getTime(user.getPlayedTime(config.getTotalTime(), 0)); }
            if (result.equalsIgnoreCase("afkTime")) { value = TimeUtil.getTime(user.getAfkTime(config.getTotalTime(), 0)); }

            topMessage.set(position, PlaceholderAPI.setPlaceholders((Player) sender, StringUtil.colorize(MessageConfig.get().getString("top." + result + ".format")))
                    .replace("%position%", position + "")
                    .replace("%player%", user.getName())
                    .replace("%value%", value)
                    .replace("%count%", String.valueOf(count)));
        }
        topMessage.add(footer.replace("%type_uppercase%", type.toUpperCase()).replace("%type_lowercase%", type.toLowerCase()).replace("%count%", String.valueOf(count)));

        for (String line : topMessage) {
            sender.sendMessage(PlaceholderAPI.setPlaceholders((Player) sender, StringUtil.colorize(line)));
        }

    }
    private void processPlayerCommand(CommandSender sender, String playerName, int period, int offset) {
        User user = userService.getUser(playerName);
        if (user == null) {
            sender.sendMessage(config.getMessage("playerNotFound"));
            return;
        }
        double points = user.getPoints(period, offset);
        long playTime = user.getPlayedTime(period, offset);
        long afkTime = user.getAfkTime(period, offset);

        for (String line : MessageConfig.get().getStringList("playerStats")) {
            sender.sendMessage(PlaceholderAPI.setPlaceholders(Bukkit.getPlayer(playerName), StringUtil.colorize(line.replace("%player%", playerName)
                    .replace("%points%", NumberUtil.round(points, 5))
                    .replace("%time%", TimeUtil.getTime(playTime))
                    .replace("%afktime%", TimeUtil.getTime(afkTime))
                    .replace("%activetime%", TimeUtil.getTime(playTime - afkTime))
                    .replace("%index%", user.getIndexAsString(period, offset))
                    .replace("{prm1}", period + "")
                    .replace("{prm2}", offset + ""))));
        }
        fileLogger.log("/index player " + playerName + " " + period + " " + offset, sender.getName());
    }

    private void deleteAllStats(int daysAgo, int durationDays, boolean all, boolean sessionDelete) {
        List<User> users;
        if (all) {
            users = userService.users();
        } else {
            users = Bukkit.getOnlinePlayers().stream().map(p -> userService.getUser(p.getName())).filter(Objects::nonNull).collect(Collectors.toList());
        }
        for (User user : users) {
            deleteStats(user, daysAgo, durationDays, sessionDelete);
        }
    }

    private void deleteStats(User user, int daysAgo, int durationDays, boolean sessionDelete) {
        if (daysAgo == -1 && durationDays == -1 && user.getActiveSession() == null) {
            userService.deleteUser(user);
            return;
        }

        List<UserSession> sessions = user.getSessions(durationDays != -1 ? 0 : daysAgo, durationDays);

        for (UserSession session : sessions) {
            if (user.getActiveSession() != null && session == user.getActiveSession()) {
                session.setPoints(0.0);
                session.setStartPlay(System.currentTimeMillis());
                session.setAfkTime(0);

                AfkUtil.clearCache(user.getName());
                continue;
            }
            user.getSessions().remove(session);
        }

        if (sessionDelete) {
            user.getSessions().clear();
        }
        userService.saveUser(user);
    }

    private void addPoints(User user, double points) {
        if (user.getActiveSession() != null) {
            user.getActiveSession().addPoints(points);

        } else {
            try {
                UserSession userSession = new ArrayList<>(user.getSessions()).get(user.getSessions().size() - 1);
                userSession.addPoints(points);

                userService.saveSession(userSession);
            }catch (IndexOutOfBoundsException ignored) {

            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(Arrays.asList("reload", "debug", "delete", "addpoint", "player", "top", "average", "sort", "hologram"));
        } else if (args.length == 2) {
            String subCommand = args[0];
            if (subCommand.equalsIgnoreCase("debug") || subCommand.equalsIgnoreCase("delete") || subCommand.equalsIgnoreCase("addpoint")) {
                List<String> completions = new ArrayList<>();
                completions.add("#all");
                completions.add("#online");
                userService.users().forEach(user -> completions.add(user.getName()));
                return completions;
            }
            if (subCommand.equalsIgnoreCase("player")) {
                List<String> completions = new ArrayList<>();
                userService.users().forEach(user -> completions.add(user.getName()));
                return completions;
            }

            if (subCommand.equalsIgnoreCase("top")) {
                return Arrays.asList("index", "time", "points", "afkTime", "activeTime");
            }

            if (subCommand.equalsIgnoreCase("hologram")) {
                return Arrays.asList("create", "remove", "restore", "kill");
            }

            if (subCommand.equalsIgnoreCase("average")) {
                return Arrays.asList("index", "time", "points", "afkTime", "activeTime");
            }

            if (subCommand.equalsIgnoreCase("sort")) {
                return Arrays.asList("best", "worst");
            }

        }
        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("top") && !args[1].isEmpty()) {
                return Arrays.asList("best", "worst");
            }

            if (args[0].equalsIgnoreCase("sort") && !args[1].isEmpty()) {
                return Arrays.asList("10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "#all");
            }

            if (args[0].equalsIgnoreCase("hologram") && args[1].equalsIgnoreCase("remove")) {
                return HologramsConfig.get().getConfigurationSection("holograms").getKeys(false).stream().toList();
            }
        }
        else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("top") && !args[2].isEmpty()) {
                return Arrays.asList("10", "15", "20", "30", "50", "#all");
            }
            if (args[0].equalsIgnoreCase("sort") && !args[2].isEmpty()) {
                return MenusConfig.get().getStringList("Placeholders-TAB");
            }
            if (args[0].equalsIgnoreCase("hologram") && args[1].equalsIgnoreCase("create") && !args[2].isEmpty()) {
                return MenusConfig.get().getStringList("Placeholders-TAB");
            }
        }

        return Collections.emptyList();
    }
}
