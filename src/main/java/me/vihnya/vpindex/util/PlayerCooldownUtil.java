package me.vihnya.vpindex.util;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import org.bukkit.entity.Player;

@UtilityClass
@SuppressWarnings("unused")
public class PlayerCooldownUtil {

    private final Table<String, String, Long> COOLDOWN_TABLE = HashBasedTable.create();

    public void putCooldown(@NonNull String delayName, @NonNull Player player, long mills) {
        COOLDOWN_TABLE.put(delayName, player.getName(), System.currentTimeMillis() + mills);
    }

    public long getCooldown(@NonNull String delayName, @NonNull Player player) {
        Long playerDelay = COOLDOWN_TABLE.get(delayName, player.getName());
        return playerDelay == null ? 0 : playerDelay - System.currentTimeMillis();
    }

    public void removeCooldown(@NonNull String delayName, @NonNull Player player) {
        COOLDOWN_TABLE.remove(delayName, player.getName());
    }

    public boolean hasCooldown(@NonNull String delayName, @NonNull Player player) {
        return getCooldown(delayName, player) > 0;
    }

}
