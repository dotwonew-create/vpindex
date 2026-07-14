package me.vihnya.vpindex.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import me.vihnya.vpindex.event.EventProperties;
import me.vihnya.vpindex.event.EventType;
import me.vihnya.vpindex.event.Parameter;
import me.vihnya.vpindex.util.StringUtil;
import me.vihnya.vpindex.util.TimeUtil;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Getter
@Setter
@SuppressWarnings("all")
public class Config {

    private final Plugin plugin;

    private Map<EventType, EventProperties> events;

    private long clearTime;

    private int totalTime;

    private boolean whitelist;

    private long updateTime;

    private long afkTime;

    private boolean debugMode = false;

    public void loadValues() {
        events = new HashMap<>();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection eventSection = config.getConfigurationSection("events");
        for (String key : eventSection.getKeys(false)) {
            EventType eventType = EventType.valueOf(key.toUpperCase());

            List<Parameter> additionalParameters = new ArrayList<>();
            ConfigurationSection section = plugin.getConfig().getConfigurationSection("events." + key);
            Parameter defaultParameter = getParameter(section, "default");
            if (defaultParameter == null) {
                plugin.getLogger().warning("Событие " + key + " не имеет параметра по умолчанию");
                continue;
            }
            for (String parameter : section.getKeys(false)) {
                boolean enable = section.getBoolean(parameter + ".enable", defaultParameter.isEnabled());
                double points = section.getDouble(parameter + ".points", defaultParameter.getPoints());
                long time;
                String configTime = section.getString(parameter + ".time");
                if (configTime == null) {
                    time = defaultParameter.getTime();
                } else {
                    time = (TimeUtil.parseTime(configTime) / 1000);
                }
                double minimum = section.getDouble(parameter + ".minimum", defaultParameter.getMinimum());

                String configReset = section.getString(parameter + ".reset");
                long reset;
                if (configReset == null) {
                    reset = defaultParameter.getReset();
                } else {
                    reset = TimeUtil.parseTime(configReset) / 1000;
                }

                long maxtime;
                String maxtimeString = section.getString(parameter + ".maxtime");
                if (maxtimeString == null) {
                    maxtime = defaultParameter.getMaxtime();
                } else {
                    maxtime = (TimeUtil.parseTime(maxtimeString) / 1000);
                }

                double demotion = section.getDouble(parameter + ".demotion", defaultParameter.getDemotion());
                Parameter param = new Parameter(parameter, enable, points, time, minimum, reset, demotion, maxtime);

                if (parameter.equalsIgnoreCase("default")) {
                    continue;
                } else {
                    additionalParameters.add(param);
                }
            }
            EventProperties eventProperties = new EventProperties(defaultParameter, additionalParameters);
            events.put(eventType, eventProperties);
        }
        ConfigurationSection settings = config.getConfigurationSection("settings");
        clearTime = TimeUtil.parseTime(settings.getString("clear")) / 1000;
        totalTime = settings.getInt("total-time");
        whitelist = settings.getBoolean("white-list");
        updateTime = TimeUtil.parseTime(settings.getString("update-time")) / 1000;
        afkTime = TimeUtil.parseTime(settings.getString("afk-threshold"));
        plugin.getLogger().info("Информация с конфигурации загружена!");
    }

    public void reload() {
        plugin.reloadConfig();
        loadValues();
    }

    public String getMessage(String key) {
        return StringUtil.colorize(MessageConfig.get().getString(key));
    }

    private Parameter getParameter(ConfigurationSection section, String parameter) {
        boolean enable = section.getBoolean(parameter + ".enable");
        double points = section.getDouble(parameter + ".points");
        long time = (TimeUtil.parseTime(section.getString(parameter + ".time")) / 1000);
        double minimum = section.getDouble(parameter + ".minimum");
        long reset = TimeUtil.parseTime(section.getString(parameter + ".reset")) / 1000;
        double demotion = section.getDouble(parameter + ".demotion");
        long maxtime = -1;
        String maxtimeString = section.getString(parameter + ".maxtime");
        if (maxtimeString != null) {
            maxtime = (TimeUtil.parseTime(maxtimeString) / 1000);
        }
        return new Parameter(parameter, enable, points, time, minimum, reset, demotion, maxtime);
    }
}
