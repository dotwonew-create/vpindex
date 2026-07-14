package me.vihnya.vpindex.menu.api;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerMenuUtility {

    @Getter
    @Setter
    private Player owner;
    @Setter
    @Getter
    private OfflinePlayer playerToKill;

    public PlayerMenuUtility(Player p) {
        this.owner = p;
    }

}
