package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class UserSession {
    private static UserSession instance;
    private String sessionId;
    private int userId;
    private String userFirstName;
    private String userMiddleName;
    private String userLastName;
    private String userPosition;
    private String userPic;
    private String userEmail;
    private String userPassword;
    private int userDepartment;
    private EmailCredentials emailCredentials;
    private User user;

    public static void setInstance(UserSession instance) {
        UserSession.instance = instance;
    }

    public UserSession(String sessionId, int userId, String userFirstName, String userMiddleName, String userLastName, String userPosition, String userPic, User user) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.userFirstName = userFirstName;
        this.userMiddleName = userMiddleName;
        this.userLastName = userLastName;
        this.userPosition = userPosition;
        this.userPic = userPic;
        this.user = user;
    }

    private UserSession() {
        sessionId = UUID.randomUUID().toString();
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return sessionId != null && !sessionId.isEmpty();
    }

    public void logout() {
        sessionId = null;
    }


    // Add other user-related getters and setters here
}
