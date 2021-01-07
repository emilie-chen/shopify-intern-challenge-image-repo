package me.nanjingchj.imgrepo.service;

import me.nanjingchj.imgrepo.model.User;
import me.nanjingchj.imgrepo.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import java.util.*;

@Service
public class UserService {
    private final UserRepo userRepo;
    // Map token -> (id, validUntil)
    private final Map<String, Pair<String, Date>> userLoginSessions = new Hashtable<>();
    // Map id -> token
    private final Map<String, String> usernameToToken = new Hashtable<>();
    public final long TIMEOUT = 900; // 15 minutes

    @Autowired
    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public boolean userExists(String username) {
        return userRepo.findByUsername(username).isPresent();
    }

    public void createUser(String username, String passwordDigest) {
        userRepo.save(new User(username, passwordDigest));
    }

    private User getUserByName(String username) {
        Optional<User> user = userRepo.findByUsername(username);
        assert user.isPresent();
        return user.get();
    }

    /**
     * if the user is already logged in, the old session id will be invalidated
     * @param username username
     * @param passwordDigest password hash value
     * @return session ID
     */
    public String loginUser(String username, String passwordDigest) {
        User user = getUserByName(username);
        if (passwordDigest.equals(user.getPassword())) {
            // password matches
            // invalidate old token, if it exists
            if (usernameToToken.containsKey(username)) {
                userLoginSessions.remove(usernameToToken.get(username)); // remove old token
                usernameToToken.remove(username); // remove user from table of logged in users
            }
            String token = UUID.randomUUID().toString();
            Date validUntilTime = new Date(new Date().getTime() + 1000 * TIMEOUT);
            userLoginSessions.put(token, Pair.of(user.getId(), validUntilTime));
            usernameToToken.put(username, token);
            return token;
        }
        return null;
    }

    public boolean isUserLoggedInById(String id) {
        if (!usernameToToken.containsKey(id)) {
            return false;
        }
        String token = usernameToToken.get(id);
        return isUserLoggedInByToken(token);
    }

    public Cookie createSessionCookie(String token) {
        Cookie cookie = new Cookie("token", token);
        cookie.setMaxAge((int) TIMEOUT);
        return cookie;
    }

    public boolean isUserLoggedInByToken(String token) {
        if (!userLoginSessions.containsKey(token)) {
            return false;
        }
        Pair<String, Date> idAndExpiryDate = userLoginSessions.get(token);
        Date currentTime = new Date();
        if (idAndExpiryDate.getSecond().after(currentTime)) {
            return true;
        }
        userLoginSessions.remove(token);
        String name = idAndExpiryDate.getFirst();
        usernameToToken.remove(name);
        return false;
    }

    public Date getSessionExpiry(String token) {
        assert userLoginSessions.containsKey(token);
        return userLoginSessions.get(token).getSecond();
    }

    /**
     * set the token expiry time to current time plus 15 minutes, if the user is logged in
     * @param token session ID
     */
    public void  refreshTokenExpiry(String token) {
        assert isUserLoggedInByToken(token);
        Pair<String, Date> idAndExpiryDate = userLoginSessions.get(token);
        Pair<String, Date> idAndNewExpiry = Pair.of(idAndExpiryDate.getFirst(), new Date(new Date().getTime() + 1000 * TIMEOUT));
        userLoginSessions.put(token, idAndNewExpiry);
    }

    public String getIdFromToken(String token) {
        return userLoginSessions.get(token).getFirst();
    }
}
