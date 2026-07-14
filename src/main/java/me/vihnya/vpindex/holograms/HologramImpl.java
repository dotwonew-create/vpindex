package me.vihnya.vpindex.holograms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import me.clip.placeholderapi.PlaceholderAPI;
import me.vihnya.vpindex.VPIndex;
import me.vihnya.vpindex.config.HologramsConfig;
import me.vihnya.vpindex.config.MessageConfig;
import me.vihnya.vpindex.holograms.Hologram;
import me.vihnya.vpindex.user.User;
import me.vihnya.vpindex.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.Plugin;

public class HologramImpl implements Hologram {

    @Override
    public void createHologram(String id, double x, double y, double z, World world) {
        Location location = new Location(world, x, y, z);
        TextDisplay textDisplay = location.getWorld().spawn(location, TextDisplay.class);
        textDisplay.setBillboard(Display.Billboard.CENTER);
        textDisplay.setAlignment(TextDisplay.TextAlignment.CENTER);
        textDisplay.setText(String.join("\n", this.getText(id)));
        for (String tag : HologramsConfig.get().getStringList("basicTags")) {
            textDisplay.addScoreboardTag(tag);
        }
        textDisplay.addScoreboardTag(id);
        this.update(id, 20L * HologramsConfig.get().getLong("holograms." + id + ".settings.update"));
    }

    @Override
    public void removeHologram(String id, Player player) {
        boolean exist = false;
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getType() != EntityType.TEXT_DISPLAY) continue;
                if (entity.getScoreboardTags().contains(id)) {
                    entity.getScoreboardTags().remove(id);
                    entity.remove();
                    HologramsConfig.get().set(id, null);
                    HologramsConfig.reload();
                    HologramsConfig.save();
                    exist = true;
                    continue;
                }
                exist = false;
            }
        }
        if (exist) {
            player.sendMessage(PlaceholderAPI.setPlaceholders(player, Objects.requireNonNull(StringUtil.colorize(Objects.requireNonNull(MessageConfig.get().getString("hologramDeleted")).replace("%hologram%", id)))));
        }
        if (!exist) {
            player.sendMessage(PlaceholderAPI.setPlaceholders(player, Objects.requireNonNull(StringUtil.colorize(Objects.requireNonNull(MessageConfig.get().getString("noHologram")).replace("%hologram%", id)))));
        }
    }

    @Override
    public void reload() {
        for (String key : HologramsConfig.get().getKeys(false)) {
            for (TextDisplay textDisplay : this.getDisplay(key)) {
                textDisplay.setText(String.join((CharSequence)"\n", this.getText(this.getTag((Entity)textDisplay))));
            }
        }
    }

    @Override
    public List<TextDisplay> getDisplay(String id) {
        ArrayList<TextDisplay> displays = new ArrayList<TextDisplay>();
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getType() != EntityType.TEXT_DISPLAY || !entity.getScoreboardTags().contains(id)) continue;
                TextDisplay textDisplay = (TextDisplay)entity;
                displays.add(textDisplay);
            }
        }
        return displays;
    }

    @Override
    public List<String> getText(String id) {
        ArrayList<String> header = new ArrayList<String>();
        ArrayList<String> footer = new ArrayList<String>();
        for (String line : HologramsConfig.get().getStringList("holograms." + id + ".text.header.lines")) {
            header.add(StringUtil.colorize(line));
        }
        for (String line : HologramsConfig.get().getStringList("holograms." + id + ".text.footer.lines")) {
            footer.add(StringUtil.colorize(line));
        }
        ArrayList<String> text = new ArrayList<String>();
        text.addAll(header);
        int count = HologramsConfig.get().getInt("holograms." + id + ".settings.players");
        String type = HologramsConfig.get().getString("holograms." + id + ".text.middle.type");
        String placeholder = HologramsConfig.get().getString("holograms." + id + ".text.middle.placeholder");
        ArrayList<Player> players = new ArrayList<Player>(Bukkit.getServer().getOnlinePlayers());
        ArrayList<User> users = new ArrayList<User>(VPIndex.getService().users());
        players.sort(Comparator.comparing(player -> PlaceholderAPI.setPlaceholders((Player)player, (String)placeholder).replace(",", ".")));
        Collections.reverse(players);
        users.sort(Comparator.comparing(user -> PlaceholderAPI.setPlaceholders((OfflinePlayer)Bukkit.getOfflinePlayer((String)user.getName()), (String)placeholder).replace(",", ".")));
        if (type.equalsIgnoreCase("best")) {
            Collections.reverse(users);
            Collections.reverse(players);
        } else if (!type.equalsIgnoreCase("worst")) {
            // empty if block
        }
        for (int j = 0; j < users.size(); ++j) {
            text.add(" ");
        }
        if (users != null && !users.isEmpty()) {
            for (int i = 0; i < count && i < users.size(); ++i) {
                if (users.get(i) == null) continue;
                for (String line : HologramsConfig.get().getStringList("holograms." + id + ".text.middle.lines")) {
                    text.set(i + header.size(), StringUtil.colorize(line).replace("%value%", PlaceholderAPI.setPlaceholders((OfflinePlayer)Bukkit.getOfflinePlayer((String)users.get(i).getName()), (String)placeholder)).replace("%position%", String.valueOf(i + 1)).replace("%playerName%", users.get(i).getName()));
                }
            }
        }
        text.addAll(footer);
        return text;
    }

    @Override
    public void update(String key, long delay) {
        Bukkit.getScheduler().runTaskTimer((Plugin)VPIndex.getPlugin(VPIndex.class), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player == null) continue;
                for (TextDisplay textDisplay : this.getDisplay(key)) {
                    textDisplay.setText(String.join((CharSequence)"\n", this.getText(this.getTag((Entity)textDisplay))));
                }
            }
        }, 0L, delay);
    }

    @Override
    public void addToConfig(String id, String placeholder) {
        HologramsConfig.get().addDefault("holograms." + id + ".settings.update", (Object)5);
        HologramsConfig.get().addDefault("holograms." + id + ".settings.players", (Object)100);
        HologramsConfig.get().addDefault("holograms." + id + ".text.header.lines", List.of("&f=========================="));
        HologramsConfig.get().addDefault("holograms." + id + ".text.middle.lines", List.of("&a%position% &f| &a%playerName% &f| &c%value%"));
        HologramsConfig.get().addDefault("holograms." + id + ".text.middle.placeholder", (Object)placeholder);
        HologramsConfig.get().addDefault("holograms." + id + ".text.middle.type", (Object)"best");
        HologramsConfig.get().addDefault("holograms." + id + ".text.footer.lines", List.of("&f=========================="));
        HologramsConfig.get().options().copyDefaults(true);
        HologramsConfig.save();
    }

    @Override
    public String getTag(Entity entity) {
        String entityTag = null;
        for (String tag : entity.getScoreboardTags()) {
            if (!HologramsConfig.get().getConfigurationSection("holograms").getKeys(false).contains(tag)) continue;
            entityTag = tag;
        }
        return entityTag;
    }
}
