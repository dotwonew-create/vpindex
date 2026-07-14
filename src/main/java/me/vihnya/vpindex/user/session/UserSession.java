package me.vihnya.vpindex.user.session;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.Setter;

import me.vihnya.vpindex.user.User;

@Getter
@Setter
@DatabaseTable(tableName = "users_session")
public class UserSession {

    public UserSession() {

    }

    public UserSession(long startPlay) {
        this.startPlay = startPlay;
    }

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(foreign = true, canBeNull = false, foreignAutoRefresh = true)
    private User user;

    @DatabaseField
    private long played;

    @DatabaseField
    private double points = 0;

    private long startPlay;

    @DatabaseField
    private long time;

    @DatabaseField
    private long afkTime;

    public void addPoints(double point) {
        this.points += point;
    }

}
