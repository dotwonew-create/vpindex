package me.vihnya.vpindex.menu.api;

import lombok.Getter;

import me.clip.placeholderapi.PlaceholderAPI;

import me.vihnya.vpindex.VPIndex;
import me.vihnya.vpindex.config.MenusConfig;
import me.vihnya.vpindex.util.StringUtil;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class PaginatedMenu extends Menu {

    protected int page = 0;
    @Getter
    protected int maxItemsPerPage = 45;
    protected int index = 0;

    public PaginatedMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    public void addMenuBorder() {
        List<String> lore = new ArrayList<>();
        String loreLine;
        ConfigurationSection section = MenusConfig.get().getConfigurationSection("SortMenu.Items");
        for (String key : section.getKeys(false)) {
            final ItemStack itemTag = new ItemStack(Material.valueOf(section.getString(key + ".Material")), Integer.parseInt(Objects.requireNonNull(section.getString(key + ".Amount"))));
            final ItemMeta meta = itemTag.getItemMeta();
            meta.setDisplayName(StringUtil.colorize(section.getString(key + ".DisplayName")));
            for (String line : section.getStringList(key + ".Lore")) {
                loreLine = PlaceholderAPI.setPlaceholders(this.playerMenuUtility.getOwner(), StringUtil.colorize(line));
                lore.add(loreLine);
            }
            meta.setLore(lore); lore.clear();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemTag.setItemMeta(meta);
            this.inventory.setItem(Integer.parseInt(Objects.requireNonNull(section.getString(key + ".Slot"))), itemTag);
        }
    }
}
