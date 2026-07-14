package me.vihnya.vpindex.config;

import me.vihnya.vpindex.VPIndex;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class MenusConfig {
    public static void createConfig() {
        if(!VPIndex.getPlugin(VPIndex.class).getDataFolder().exists()) VPIndex.getPlugin(VPIndex.class).getDataFolder().mkdirs();

        messageFile = new File(VPIndex.getPlugin(VPIndex.class).getDataFolder(), "menus.yml"); messageConfig = new YamlConfiguration();

        if(!messageFile.exists()) VPIndex.getPlugin(VPIndex.class).saveResource("menus.yml", true);

        loadConfig(messageFile, messageConfig);

    }

    public static void reload() {
        messageConfig = YamlConfiguration.loadConfiguration(messageFile);
    }

    private static void loadConfig(File file, FileConfiguration config) {
        try {
            config.load(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static FileConfiguration get() {
        return messageConfig;
    }

    public static void save(){
        try{
            messageConfig.save(messageFile);
        }catch (IOException e){
            System.out.println("Couldn't save file");
        }
    }
    private static File messageFile;
    private static FileConfiguration messageConfig;




}