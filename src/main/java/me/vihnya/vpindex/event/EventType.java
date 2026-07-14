package me.vihnya.vpindex.event;

public enum EventType {
    BLOCK_BREAK,
    BLOCK_PLACE,
    CRAFT,
    PICKUP,
    KILL_MOB,
    DAMAGE,
    GET_DAMAGE,
    JOIN,
    QUIT,
    CHANGE_WORLD,
    ENTER_BED,
    FISHING,
    CHAT,
    EXP,
    JUMP,
    DEATH,
    KILL_PLAYER,
    ENCHANT,
    FOOD,
    INVENTORY_OPEN,
    INTERACT_BLOCK,
    RAID_TRIGGER,
    RAID_WIN,
    MOVE;

    public String getCooldownKey(Parameter parameter) {
        return name() + "|" + parameter.getData();
    }
}
