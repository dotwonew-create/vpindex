package me.vihnya.vpindex.afk;

public interface AfkService {

    long getAfkTime(String name);

    long getLastMoveTime(String name);

    long getAfkThreshold();

    boolean isAfk(String name);

    void setAfk(String name, boolean afk);

    void clearCache(String name);
}
