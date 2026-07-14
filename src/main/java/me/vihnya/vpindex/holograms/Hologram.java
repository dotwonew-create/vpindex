package me.vihnya.vpindex.holograms;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.List;

public interface Hologram {

    void createHologram(String id, double x, double y, double z, World world);
    void removeHologram(String id, Player player);
    void update(String key, long delay);
    void addToConfig(String id, String placeholder);
    void reload();

    List<TextDisplay> getDisplay(String id);
    List<String> getText(String id);

    String getTag(Entity entity);
}
