package me.vihnya.vpindex.event;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;

import lombok.RequiredArgsConstructor;

import me.vihnya.vpindex.config.Config;
import me.vihnya.vpindex.user.User;
import me.vihnya.vpindex.user.service.UserService;
import me.vihnya.vpindex.util.NumberUtil;
import me.vihnya.vpindex.util.Pair;
import me.vihnya.vpindex.util.PlayerCooldownUtil;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;

@RequiredArgsConstructor
public class EventListener implements Listener {

    private final UserService userService;

    private final Config config;

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        EventType eventType = EventType.BLOCK_BREAK;
        givePoints(e.getPlayer(), eventType, e.getBlock().getBlockData().getMaterial().name());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        EventType eventType = EventType.BLOCK_PLACE;
        givePoints(e.getPlayer(), eventType, e.getBlock().getBlockData().getMaterial().name());
    }

    @EventHandler
    public void onChange(PlayerChangedWorldEvent e) {
        givePoints(e.getPlayer(), EventType.CHANGE_WORLD, e.getPlayer().getLocation().getWorld().getName());
    }

    @EventHandler
    public void onExp(PlayerPickupExperienceEvent e) {
        givePoints(e.getPlayer(), EventType.EXP, null);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        givePoints(e.getPlayer(), EventType.JOIN, null);
    }

    @EventHandler
    public void onBed(PlayerBedEnterEvent e) {
        givePoints(e.getPlayer(), EventType.ENTER_BED, null);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        givePoints(e.getPlayer(), EventType.CHAT, null);
    }

    @EventHandler
    public void onInteractBlock(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        if (block == null) {
            return;
        }

        if (e.getHand() == EquipmentSlot.HAND) {
            givePoints(e.getPlayer(), EventType.INTERACT_BLOCK, e.getAction().name() + "_" + block.getBlockData().getMaterial().name());
        }
    }


    @EventHandler
    public void onFood(PlayerItemConsumeEvent e) {
        givePoints(e.getPlayer(), EventType.FOOD, null);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (e.getPlayer() instanceof Player) {
            givePoints((Player) e.getPlayer(), EventType.INVENTORY_OPEN, null);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        int fromX = (int) event.getFrom().getX();
        int fromY = (int) event.getFrom().getY();
        int fromZ = (int) event.getFrom().getZ();

        int toX = (int) event.getTo().getX();
        int toY = (int) event.getTo().getY();
        int toZ = (int) event.getTo().getZ();

        if (fromX != toX || fromY != toY || fromZ != toZ) {
            Player player = event.getPlayer();
            Entity entity = player.getVehicle();
            String data = null;
            if (entity != null) {
                data = entity.getType().name();
            }
            givePoints(player, EventType.MOVE, data);
        }
    }

    @EventHandler
    public void onStatistic(PlayerStatisticIncrementEvent e) {
        Player player = e.getPlayer();

        if (e.getStatistic() == Statistic.TIME_SINCE_REST) {
            return;
        }

        String data = null;
        EventType eventType = null;
        switch (e.getStatistic()) {
            case PICKUP: {
                eventType = EventType.PICKUP;
                data = getMaterialData(e.getMaterial());
                break;
            }
            case CRAFT_ITEM: {
                eventType = EventType.CRAFT;
                data = getMaterialData(e.getMaterial());
                break;
            }
            case DEATHS: {
                eventType = EventType.DEATH;
                break;
            }
            case JUMP: {
                eventType = EventType.JUMP;
                break;
            }
            case MOB_KILLS: {
                eventType = EventType.KILL_MOB;
                data = getEntityData(e.getEntityType());
                break;
            }
            case PLAYER_KILLS: {
                eventType = EventType.KILL_PLAYER;
                break;
            }
            case ITEM_ENCHANTED: {
                eventType = EventType.ENCHANT;
                data = getMaterialData(e.getMaterial());
                break;
            }
            case LEAVE_GAME: {
                eventType = EventType.QUIT;
                break;
            }
            case DAMAGE_TAKEN: {
                eventType = EventType.GET_DAMAGE;
                break;
            }
            case DAMAGE_DEALT: {
                eventType = EventType.DAMAGE;
                break;
            }
            case FISH_CAUGHT: {
                eventType = EventType.FISHING;
                break;
            }
            case RAID_TRIGGER: {
                eventType = EventType.RAID_TRIGGER;
                break;
            }
            case RAID_WIN: {
                eventType = EventType.RAID_WIN;
                break;
            }
        }
        if (eventType == null) {
            return;
        }
        givePoints(player, eventType, data);
    }

    public String getMaterialData(Material material) {
        if (material == null) {
            return null;
        }
        return material.name();
    }
    public String getEntityData(EntityType entityType) {
        if (entityType == null) {
            return null;
        }
        return entityType.name();
    }
    public void givePoints(Player player, EventType eventType, String data) {
        if (config.isWhitelist() && !player.isWhitelisted() && Bukkit.hasWhitelist()) {
            return;
        }
        EventProperties eventProperties = config.getEvents().get(eventType);

        if (eventProperties == null) {
            return;
        }
        User user = userService.getUser(player.getName());
        if (data != null && user != null) {
            user.sendDebugMessage(player, config.isDebugMode(), " ");
            user.sendDebugMessage(player, config.isDebugMode(), "&7[DEBUG] Data for event " + eventType + ": &f " + data);
            user.sendDebugMessage(player, config.isDebugMode(), " ");
        }

        Parameter defaultParameter = eventProperties.getDefaultParameter();
        Parameter targetParameter = findParameter(data, eventProperties.getAdditionalParameters());

        Parameter usableParameter = null;
        if (defaultParameter.isEnabled()) {
            usableParameter = defaultParameter;
        }
        if (targetParameter != null && targetParameter.isEnabled()) {
            usableParameter = targetParameter;
        }
        if (usableParameter == null) {
            return;
        }

        if (user != null) {

            PlayerCooldownUtil.putCooldown(eventType.getCooldownKey(usableParameter), player, usableParameter.getReset() * 1000);

            long secondsPlayed = user.getSeconds(eventType, usableParameter.getData());

            double points = usableParameter.getPoints();
            long count = secondsPlayed / usableParameter.getTime();
            double scoreReduction = count * usableParameter.getDemotion();

            if (points - scoreReduction < usableParameter.getMinimum()) {
                points = usableParameter.getMinimum();
                if (usableParameter.getMaxtime() == -1) {
                    user.setLastPoints(eventType, usableParameter.getData(), new Pair<>(-1L, true));
                } else {
                    user.setLastPoints(eventType, usableParameter.getData(), new Pair<>(usableParameter.getMaxtime(), false));
                }
            } else {
                points -= scoreReduction;
                if (usableParameter.getMaxtime() == -1) {
                    user.setLastPoints(eventType, usableParameter.getData(), new Pair<>(-1L, false));
                } else {
                    user.setLastPoints(eventType, usableParameter.getData(), new Pair<>(usableParameter.getMaxtime(), false));
                }
            }

            if (user.getActiveSession() != null) {
                user.getActiveSession().addPoints(points);
            }

            user.sendDebugMessage(player, config.isDebugMode(), " ");
            user.sendDebugMessage(player, config.isDebugMode(), "&7[DEBUG] &fВы получили " + NumberUtil.round(points, 5) + " очков за " + eventType.name());
            user.sendDebugMessage(player, config.isDebugMode(), "&7[DEBUG] &fИспользуемый параметр: " + usableParameter.getData());
            user.sendDebugMessage(player, config.isDebugMode(), " ");

        }
    }

    public Parameter findParameter(String data, List<Parameter> parameters) {
        if (data == null) {
            return null;
        }

        Parameter target = null;
        for (Parameter parameter : parameters) {
            if (parameter.getData().equalsIgnoreCase(data)) {
                target = parameter;
            }
        }
        return target;
    }
}
