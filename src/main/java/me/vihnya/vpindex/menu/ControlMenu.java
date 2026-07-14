package me.vihnya.vpindex.menu;

import me.clip.placeholderapi.PlaceholderAPI;

import me.vihnya.vpindex.config.MenusConfig;
import me.vihnya.vpindex.menu.api.Menu;
import me.vihnya.vpindex.menu.api.PlayerMenuUtility;
import me.vihnya.vpindex.util.StringUtil;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ControlMenu extends Menu
{
    public String type;
    public int count;
    public String placeholder;

    public ControlMenu(PlayerMenuUtility playerMenuUtility, final String type, final int count, final String placeholder) {
        super(playerMenuUtility);
        this.count = count;
        this.placeholder = placeholder;
        this.type = type;
    }

    @Override
    public String getMenuName() {
        return PlaceholderAPI.setPlaceholders(playerMenuUtility.getOwner(), StringUtil.colorize(MenusConfig.get().getString("ControlMenu.MenuName")));
    }

    @Override
    public int getSlots() {
        return MenusConfig.get().getInt("ControlMenu.Size");
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        for (String key : MenusConfig.get().getConfigurationSection("ControlMenu.Items").getKeys(false)) {
            if (MenusConfig.get().getConfigurationSection("ControlMenu.Items").getInt(key + ".Slot") == e.getSlot()) {
                List<String> actionsClick = MenusConfig.get().getConfigurationSection("ControlMenu.Items").getStringList(key + ".Action." + e.getClick().name() + "_CLICK");
                for (String action : actionsClick) {
                    if (action.contains("[run_command]")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(player, action.split("]")[1].substring(1)));
                    }
                    if (action.contains("[player_command]")) {
                        Bukkit.dispatchCommand(e.getWhoClicked(), PlaceholderAPI.setPlaceholders(this.playerMenuUtility.getPlayerToKill(), action.split("]")[1].replace("%player%", Objects.requireNonNull(this.playerMenuUtility.getPlayerToKill().getName())).substring(1)));
                    }
                    if (action.contains("[close]")) {
                        new SortMenu(this.playerMenuUtility, this.type, this.count, this.placeholder).open();
                    }
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        ConfigurationSection section = MenusConfig.get().getConfigurationSection("ControlMenu.Items");
        final List<String> lore = new ArrayList<>();
        String loreLine = null;
        for (String key : section.getKeys(false)) {
            ItemStack itemTag = new ItemStack(Material.valueOf(section.getString(key + ".Material")), Integer.parseInt(Objects.requireNonNull(section.getString(key + ".Amount"))));
            ItemMeta meta = itemTag.getItemMeta();
            meta.setDisplayName(StringUtil.colorize(section.getString(key + ".DisplayName")));
            for (String line : section.getStringList(key + ".Lore")) {
                loreLine = PlaceholderAPI.setPlaceholders(this.playerMenuUtility.getOwner(), StringUtil.colorize(line));
                lore.add(loreLine);
            }
            meta.setLore(lore);
            lore.clear();
            if (MenusConfig.get().getString(section + key + ".HideAttributes") != null && MenusConfig.get().getBoolean(section + key + ".HideAttributes")) {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }
            itemTag.setItemMeta(meta);
            this.inventory.setItem(Integer.parseInt(Objects.requireNonNull(section.getString(key + ".Slot"))), itemTag);
        }
    }
}
