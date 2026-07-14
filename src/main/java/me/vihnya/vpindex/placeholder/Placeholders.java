package me.vihnya.vpindex.placeholder;

import lombok.RequiredArgsConstructor;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import me.vihnya.vpindex.config.Config;
import me.vihnya.vpindex.event.UpdateRunnable;
import me.vihnya.vpindex.user.User;
import me.vihnya.vpindex.user.service.UserService;
import me.vihnya.vpindex.util.AfkUtil;
import me.vihnya.vpindex.util.NumberUtil;

import org.bukkit.OfflinePlayer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class Placeholders extends PlaceholderExpansion {

    private final UserService userService;

    private final Config config;

    private  final UpdateRunnable updateRunnable;

    @Override
    public @NotNull String getIdentifier() {
        return "activity";
    }

    @Override
    public @NotNull String getAuthor() {
        return "vihnya";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0-SNAPSHOT";
    }

    @Override
    public boolean persist() { return true; }
    @Override
    public boolean canRegister() { return true; }
    @Override
    public @Nullable String onRequest(OfflinePlayer p, @NotNull String params) {
        User user = userService.getUser(p.getName());
        if (user == null) {
            return "Empty Data";
        }
        String result = "N/A";
        String[] parts = params.split("_");
        String parameter = parts.length == 0 ? params : parts[0];
        if (parameter.startsWith("time")) {
            long time = 0;
            if (parts.length == 1) {
                time = user.getPlayedTime(-1, -1);
            }
            if (parts.length == 2) {
                time = user.getPlayedTime(Integer.parseInt(parts[1]), 0);
            }
            if (parts.length == 3) {
                time = user.getPlayedTime(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            }
            switch (parameter) {
                case "time": {
                    result = formatFullTime(time);
                    break;
                }
                case "time1": {
                    result = formatDays(time, true);
                    break;
                }
                case "time2": {
                    result =  formatHours(time, true);
                    break;
                }
                case "time3": {
                    result = formatMinutes(time, true);
                    break;
                }
                case "time4": {
                    result = formatSeconds(time, true);
                    break;
                }
                case "time5": {
                    result = formatDays(time, false);
                    break;
                }
                case "time6": {
                    result =  formatHours(time, false);
                    break;
                }
                case "time7": {
                    result = formatMinutes(time, false);
                    break;
                }
                case "time8": {
                    result = formatSeconds(time, false);
                    break;
                }
            }
        }
        if (parameter.startsWith("afktime")) {
            long time = 0;
            if (parts.length == 1) {
                time = user.getAfkTime(-1, -1);
            }
            if (parts.length == 2) {
                time = user.getAfkTime(Integer.parseInt(parts[1]), 0);
            }
            if (parts.length == 3) {
                time = user.getAfkTime(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            }
            switch (parameter) {
                case "afktime": {
                    result = formatFullTime(time);
                    break;
                }
                case "afktime1": {
                    result = formatDays(time, true);
                    break;
                }
                case "afktime2": {
                    result =  formatHours(time, true);
                    break;
                }
                case "afktime3": {
                    result = formatMinutes(time, true);
                    break;
                }
                case "afktime4": {
                    result = formatSeconds(time, true);
                    break;
                }
                case "afktime5": {
                    result = formatDays(time, false);
                    break;
                }
                case "afktime6": {
                    result =  formatHours(time, false);
                    break;
                }
                case "afktime7": {
                    result = formatMinutes(time, false);
                    break;
                }
                case "afktime8": {
                    result = formatSeconds(time, false);
                    break;
                }


            }
        }
        if (parameter.startsWith("activetime")) {
            long time = 0;
            if (parts.length == 1) {
                time = user.getPlayedTime(-1, -1) - user.getAfkTime(-1, -1);
            }
            if (parts.length == 2) {
                time =  user.getPlayedTime(Integer.parseInt(parts[1]), 0) - user.getAfkTime(Integer.parseInt(parts[1]), 0);
            }
            if (parts.length == 3) {
                time = user.getPlayedTime(Integer.parseInt(parts[1]), Integer.parseInt(parts[2])) - user.getAfkTime(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            }
            switch (parameter) {
                case "activetime": {
                    result = formatFullTime(time);
                    break;
                }
                case "activetime1": {
                    result = formatDays(time, true);
                    break;
                }
                case "activetime2": {
                    result =  formatHours(time, true);
                    break;
                }
                case "activetime3": {
                    result = formatMinutes(time, true);
                    break;
                }
                case "activetime4": {
                    result = formatSeconds(time, true);
                    break;
                }
                case "activetime5": {
                    result = formatDays(time, false);
                    break;
                }
                case "activetime6": {
                    result =  formatHours(time, false);
                    break;
                }
                case "activetime7": {
                    result = formatMinutes(time, false);
                    break;
                }
                case "activetime8": {
                    result = formatSeconds(time, false);
                    break;
                }


            }
        }
        switch (parameter.toLowerCase()) {
            case "points": {
                if (parts.length == 1) {
                    result = NumberUtil.round(user.getPoints(-1, -1), 2);
                }
                if (parts.length == 2) {
                    result = NumberUtil.round(user.getPoints(Integer.parseInt(parts[1]), 0), 2);
                }
                if (parts.length == 3) {
                    result = NumberUtil.round(user.getPoints(Integer.parseInt(parts[1]), Integer.parseInt(parts[2])), 2);
                }
                break;
            }
            case "index": {
                if (parts.length == 1) {
                    result =  user.getIndexAsString(config.getTotalTime(), 0);
                }
                if (parts.length == 2) {
                    result =  user.getIndexAsString(Integer.parseInt(parts[1]), 0);
                }
                if (parts.length == 3) {
                    result = user.getIndexAsString(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                }
                break;
            }
            case "indext": {
                result = updateRunnable.getUserIndex(user);
                break;
            }
            case "afk": {
                result = AfkUtil.isAfk(user) ? config.getMessage("is-afk") : config.getMessage("is-not-afk");
                break;
            }
        }
        return result;
    }

    public static String formatFullTime(long seconds) {
        long days = TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - TimeUnit.DAYS.toHours(days);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(seconds));
        return String.format("%dд %dч %dм", days, hours, minutes);
    }

    public static String formatDays(long seconds, boolean with) {
        long days = TimeUnit.SECONDS.toDays(seconds);
        if (!with) {
            return String.valueOf(days);
        }
        return String.format("%dд", days);
    }

    public static String formatHours(long seconds, boolean with) {
        long hours = TimeUnit.SECONDS.toHours(seconds);
        if (!with) {
            return String.valueOf(hours);
        }
        return String.format("%dч", hours);
    }

    public static String formatMinutes(long seconds, boolean with) {
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        if (!with) {
            return String.valueOf(minutes);
        }
        return String.format("%dм", minutes);
    }

    public static String formatSeconds(long seconds, boolean with) {
        if (!with) {
            return String.valueOf(seconds);
        }
        return String.format("%dс", seconds);
    }
}
