package me.vihnya.vpindex.user;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.Setter;

import me.vihnya.vpindex.event.EventType;
import me.vihnya.vpindex.user.session.UserSession;
import me.vihnya.vpindex.util.AfkUtil;
import me.vihnya.vpindex.util.NumberUtil;
import me.vihnya.vpindex.util.Pair;
import me.vihnya.vpindex.util.StringUtil;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@DatabaseTable(tableName = "users")
public class User {

    public User(String name) {
        this.name = name;
    }

    public User() {

    }

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(unique = true, canBeNull = false)
    private String name;


    @ForeignCollectionField(eager = true)
    private ForeignCollection<UserSession> sessions;

    private boolean online;

    private boolean debugMode = false;

    private UserSession activeSession;

    private Map<String, Long> eventMap = new HashMap<>();

    private Map<String, Pair<Long, Boolean>> pointsMap = new HashMap<>();

    public void setLastPoints(EventType eventType, String data, Pair<Long, Boolean> lastPoints) {
        pointsMap.put(eventType + "|" + data, lastPoints);
    }

    public Pair<Long, Boolean> getLastPoints(EventType eventType, String data) {
        return pointsMap.get(eventType + "|" + data);
    }
    public void addSeconds(EventType eventType, String data, long seconds) {
        eventMap.put(eventType + "|" + data, getSeconds(eventType, data) + seconds);
    }


    public long getSeconds(EventType eventType, String data) {
        Long seconds = eventMap.get(eventType + "|" + data);
        if (seconds == null) {
            eventMap.put(eventType + "|" + data, 0L);
            return 0L;
        }
        return seconds;
    }

    public void setSeconds(EventType eventType, String data, long seconds) {
        eventMap.put(eventType + "|" + data, seconds);
    }

    public long getPlayedTime(int days, int offset) {
        long playedTime = 0;

        for (UserSession session : getSessions(offset, days)) {
            if (session == activeSession) {
                playedTime += (System.currentTimeMillis() - activeSession.getStartPlay()) / 1000;
                continue;
            }
            playedTime += session.getPlayed();
        }
        return playedTime;
    }

    public long getAfkTime(int days, int offset) {
        long afkTime = 0;

        for (UserSession session : getSessions(offset, days)) {
            if (session == activeSession) {
                afkTime += AfkUtil.getAfkTime(this);
                continue;
            }
            afkTime += session.getAfkTime();
        }

        return afkTime;
    }

    public List<UserSession> getSessions(int offset, int durationDays) {
        List<UserSession> sessionList = new ArrayList<>();
        if (durationDays == -1 && offset == -1) {
            if (activeSession != null) {
                sessionList.add(activeSession);
            }
            sessionList.addAll(sessions);
            return sessionList;
        }

        long currentTime = System.currentTimeMillis();
        long millisecondsInDay = 24 * 60 * 60 * 1000;

        long endDate = currentTime - offset * millisecondsInDay;
        long startDate = endDate - durationDays * millisecondsInDay;

        for (UserSession session : sessions) {
            long sessionTime = session.getTime();
            if (sessionTime >= startDate && sessionTime <= endDate) {
                sessionList.add(session);
            }
        }
        if (activeSession != null && offset == 0) {
            sessionList.add(activeSession);
        }

        return sessionList;
    }


    public double getPoints(int days, int offset) {
        double points = 0.0;

        for (UserSession session : getSessions(offset, days)) {
            points += session.getPoints();
        }
        return points;
    }

    public void sendDebugMessage(Player player, boolean allDebug, String message) {
        if ((debugMode && player != null) || (allDebug && player != null)) {
            player.sendMessage(StringUtil.colorize(message));
        }
    }

    public String getIndexAsString(int days, int offset) {
        double index = getIndex(days, offset);
        return NumberUtil.round(index, 2);
    }

    public String getPointsAsString(int days, int offset) {
        double points = getPoints(days, offset);
        return NumberUtil.round(points, 2);
    }

    public UserSession findSession(int id) {
        for (UserSession session : getSessions()) {
            if (session.getId() == id) {
                return session;

            }
        }
        return null;
    }
    public double getIndex(int days, int offset) {
        double index = getPoints(days, offset) / (getPlayedTime(days, offset) - getAfkTime(days, offset));
        if (Double.isNaN(index)) {
            return 0.0;
        } else {
            return index;
        }
    }
}
