package me.vihnya.vpindex.user.service;

import com.j256.ormlite.dao.Dao;
import lombok.RequiredArgsConstructor;
import me.vihnya.vpindex.user.User;
import me.vihnya.vpindex.user.session.UserSession;
import me.vihnya.vpindex.util.AfkUtil;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.*;

@RequiredArgsConstructor
public clasUserServiceImpl implements UserService {

    private final Map<String, User> loadedUsers = new HashMap<>();

    private final Dao<User, Long> userDao;

    private final Dao<UserSession, Long> userSessionDao;

    @Override
    public List<User> cachedUsers() {
        return new ArrayList<>(loadedUsers.values());
    }

    @Override
    public User getUser(String name) {

        User loadedUser = loadedUsers.get(name);
        if (loadedUser != null) {
            return loadedUser;
        }

        return loadUser(name);
    }

    @Override
    public User loadUser(String name) {
        List<User> users;
        try {
            users = userDao.queryForEq("name", name);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Optional<User> userOptional = users.stream().findFirst();
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setOnline(Bukkit.getPlayer(name) != null);

            loadedUsers.put(name, user);
            return user;
        }

        return null;
    }

    @Override
    public void saveUser(User user) {
        try {
            userDao.update(user);
            //loadedUsers.remove(user.getName());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveUserSession(User user) {
        UserSession userSession = user.getActiveSession();
        userSession.setPlayed(((System.currentTimeMillis() - userSession.getStartPlay()) / 1000));
        userSession.setTime(System.currentTimeMillis());
        userSession.setAfkTime(AfkUtil.getAfkTime(user));

        user.setActiveSession(null);
        user.getSessions().add(userSession);
    }

    @Override
    public void createUser(User user) {
        try {
            userDao.assignEmptyForeignCollection(user, "sessions");
            userDao.create(user);
            loadedUsers.put(user.getName(), user);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteUser(User user) {
        try {
            userDao.delete(user);
            loadedUsers.remove(user.getName());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveSession(UserSession session) {
        try {
            userSessionDao.update(session);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteSessions(User user) {
        try {
            for (UserSession session : user.getSessions()) {
                userSessionDao.delete(session);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<User> users() {
        try {
            List<User> users = userDao.queryForAll();
            List<User> userList = new ArrayList<>();
            for (User user : users) {
                if (isLoaded(user)) {
                    userList.add(getUser(user.getName()));
                } else {
                    userList.add(user);
                }
            }
            return userList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private boolean isLoaded(User user) {
        return loadedUsers.containsKey(user.getName());
    }
}
