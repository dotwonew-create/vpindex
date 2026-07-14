package me.vihnya.vpindex.util;

import lombok.experimental.UtilityClass;

import me.vihnya.vpindex.afk.AfkService;
import me.vihnya.vpindex.user.User;

@UtilityClass
public class AfkUtil {

    private static AfkService afkService;
    public void init(AfkService afkService) {
        AfkUtil.afkService = afkService;
    }

    public long getAfkTime(User user) {
       return afkService.getAfkTime(user.getName());
    }

    public boolean isAfk(User user) {
        return afkService.isAfk(user.getName());
    }

    public void clearCache(String name) {
        afkService.clearCache(name);
    }
}
