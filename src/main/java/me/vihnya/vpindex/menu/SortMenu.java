package me.vihnya.vpindex.menu;

import me.clip.placeholderapi.PlaceholderAPI;

import me.vihnya.vpindex.VPIndex;
import me.vihnya.vpindex.config.MenusConfig;
import me.vihnya.vpindex.config.MessageConfig;
import me.vihnya.vpindex.menu.api.PaginatedMenu;
import me.vihnya.vpindex.menu.api.PlayerMenuUtility;
import me.vihnya.vpindex.user.User;
import me.vihnya.vpindex.util.StringUtil;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getServer;

public class SortMenu extends PaginatedMenu {

    public String type;
    public int count;
    public String placeholder;

    public SortMenu(final PlayerMenuUtility playerMenuUtility, final String type, final int count, final String placeholder) {
        super(playerMenuUtility);
        this.type = type;
        this.count = count;
        this.placeholder = placeholder;
    }

    @Override
    public String getMenuName() {
        return PlaceholderAPI.setPlaceholders(playerMenuUtility.getOwner(), StringUtil.colorize(MenusConfig.get().getString("SortMenu.MenuName")));
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (e.getCurrentItem().getType().equals(Material.PLAYER_HEAD)) {
                SkullMeta meta = (SkullMeta) e.getCurrentItem().getItemMeta();
                this.playerMenuUtility.setPlayerToKill(meta.getOwningPlayer());
                new ControlMenu(this.playerMenuUtility, this.type, this.count, this.placeholder).open();
        }
        for (String key : MenusConfig.get().getConfigurationSection("SortMenu.Items").getKeys(false)) {
            if (MenusConfig.get().getConfigurationSection("SortMenu.Items").getInt(key + ".Slot") == e.getSlot()) {
                List<String> actionsClick = MenusConfig.get().getConfigurationSection("SortMenu.Items").getStringList(key + ".Action." + e.getClick().name() + "_CLICK");
                for (String action : actionsClick) {
                    if (action.contains("[run_command]")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(player, action.split("]")[1].replace("%player%", this.playerMenuUtility.getOwner().getName()).substring(1)));
                    }
                    if (action.contains("[player_command]")) {
                        Bukkit.dispatchCommand(e.getWhoClicked(), PlaceholderAPI.setPlaceholders(player, action.split("]")[1].replace("%player%", player.getName()).substring(1)));
                    }
                    if (action.contains("[close]")) {
                        player.closeInventory();
                    }
                    if (action.contains("[nextPage]")) {
                        if (this.index + 1 < VPIndex.getService().users().size()) {
                            page++;
                            super.open();
                        }
                        else {
                            player.sendMessage(StringUtil.colorize(MessageConfig.get().getString("menu.lastPage")));
                        }
                    }
                    if (action.contains("[previousPage]")) {
                        if (page == 0) {
                            player.sendMessage(StringUtil.colorize(MessageConfig.get().getString("menu.firstPage")));
                        }
                        else {
                            page--;
                            super.open();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();

        ArrayList<Player> players = new ArrayList<>(Bukkit.getServer().getOnlinePlayers());
        ArrayList<User> users = new ArrayList<>(VPIndex.getService().users());

        players.sort(Comparator.comparing(player -> PlaceholderAPI.setPlaceholders(player, this.placeholder).replace(",", ".")));
        Collections.reverse(players);

        users.sort(Comparator.comparing(user -> PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(user.getName()), this.placeholder).replace(",", ".")));

        if (this.type.equalsIgnoreCase("best")) {
            Collections.reverse(users);
            Collections.reverse(players);
        } else if (!this.type.equalsIgnoreCase("worst")) {
            return;
        }

        if (users != null && !users.isEmpty()) {
            for (int i = 0; i < this.getMaxItemsPerPage(); ++i) {
                this.index = this.getMaxItemsPerPage() * this.page + i;
                if (this.index >= users.size()) break;
                if (users.get(this.index) == null || Objects.equals(PlaceholderAPI.setPlaceholders(Objects.requireNonNull(Bukkit.getOfflinePlayer(users.get(index).getName())), this.placeholder), "")) continue;

                ItemStack playerItem = new ItemStack(Material.PLAYER_HEAD, 1);
                SkullMeta playerMeta = (SkullMeta)playerItem.getItemMeta();
                playerMeta.setOwnerProfile(Bukkit.getOfflinePlayer(users.get(this.index).getName()).getPlayerProfile());
                //playerMeta.getPersistentDataContainer().set(new NamespacedKey(VPIndex.getPlugin(VPIndex.class), "uuid"), PersistentDataType.STRING, (Object)Bukkit.getOfflinePlayer((String)users.get(this.index).getName()).getUniqueId().toString());
                playerMeta.setDisplayName(StringUtil.colorize(PlaceholderAPI.setPlaceholders(Objects.requireNonNull(Bukkit.getOfflinePlayer((String)users.get(this.index).getName())), (String)Objects.requireNonNull(MenusConfig.get().getString("SortMenu.Heads.DisplayName").replace("%target%", Objects.requireNonNull(Bukkit.getOfflinePlayer(users.get(index).getName())).getName()).replace("%target_uppercase%", Objects.requireNonNull(Bukkit.getOfflinePlayer(users.get(index).getName())).getName()).toUpperCase().replace("%value%", PlaceholderAPI.setPlaceholders(Objects.requireNonNull(Bukkit.getOfflinePlayer(users.get(index).getName())), placeholder)).replace("%position%", "" + (index + 1)).replace("%page%", String.valueOf(page))))));
                ArrayList<String> lore = new ArrayList<>();
                for (String line : MenusConfig.get().getStringList("SortMenu.Heads.Lore")) {
                    lore.add(StringUtil.colorize(PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(users.get(index).getName()), line.replace("%target%", users.get(index).getName()).replace("%target_uppercase%", users.get(index).getName().toUpperCase()).replace("%position%", "" + (index + 1)).replace("%value%", PlaceholderAPI.setPlaceholders(Objects.requireNonNull(Bukkit.getOfflinePlayer(users.get(index).getName())), placeholder)))));
                }
                playerMeta.setLore(lore); lore.clear();
                playerItem.setItemMeta(playerMeta);
                inventory.addItem(playerItem);
            }
        }
    }
}
