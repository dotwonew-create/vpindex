package me.vihnya.vpindex.user.service;

import me.vihnya.vpindex.user.User;
import me.vihnya.vpindex.user.session.UserSession;

import java.util.List;

public interface UserService {

    List<User> cachedUsers();

    User getUser(String name);
    User loadUser(String name);
    void saveUser(User user);
    void saveUserSession(User user);
    void createUser(User user);
    void deleteUser(User user);
    void saveSession(UserSession session);
    void deleteSessions(User user);
    List<User> users();


}
