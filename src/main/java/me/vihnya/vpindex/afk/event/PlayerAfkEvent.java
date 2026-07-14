package me.vihnya.vpindex.afk.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerAfkEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    public PlayerAfkEvent(Player player) {
        super(player);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
