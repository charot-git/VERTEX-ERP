package com.vertex.vos.Constructors;

import java.util.UUID;

public class UserSession {
    private static UserSession instance;
    private String sessionId;
    private int userId;
    private String userFirstName;
    private String userMiddleName;
    private String userLastName;
    private String userPosition;
    private String userPic;
    private String userDepartment;

    public static void setInstance(UserSession instance) {
        UserSession.instance = instance;
    }

    public String getUserDepartment() {
        return userDepartment;
    }

    public void setUserDepartment(String userDepartment) {
        this.userDepartment = userDepartment;
    }

    public String getUserPic() {
        return userPic;
    }

    public void setUserPic(String userPic) {
        this.userPic = userPic;
    }

    public UserSession(String sessionId, int userId, String userFirstName, String userMiddleName, String userLastName, String userPosition, String userPic) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.userFirstName = userFirstName;
        this.userMiddleName = userMiddleName;
        this.userLastName = userLastName;
        this.userPosition = userPosition;
        this.userPic = userPic;
    }

    private UserSession() {
        // Private constructor to prevent instantiation
        // Generate a unique session ID when the UserSession is created
        sessionId = UUID.randomUUID().toString();
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserMiddleName(String userMiddleName) {
        this.userMiddleName = userMiddleName;
    }

    public String getUserMiddleName() {
        return userMiddleName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserPosition(String userPosition) {
        this.userPosition = userPosition;
    }

    public String getUserPosition() {
        return userPosition;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isLoggedIn() {
        return sessionId != null && !sessionId.isEmpty();
    }

    public void logout() {
        sessionId = null;
        // Reset other user details if needed
    }
    // Add other user-related getters and setters here
}
