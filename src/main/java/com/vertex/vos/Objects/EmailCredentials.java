package com.vertex.vos.Objects;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EmailCredentials {
    private String host;
    private int port;
    private String email;
    private String password;

    // Default system email credentials
    private static final String DEFAULT_HOST = "support.men2corp.com";  // Replace with your system email SMTP server
    private static final int DEFAULT_PORT = 465;                    // Replace with the correct port (465 for SSL or 587 for TLS)
    private static final String DEFAULT_EMAIL = "vertex@support.men2corp.com";  // Replace with your system email address
    private static final String DEFAULT_PASSWORD = "VertexAccess24";        // Replace with your system email password

    public EmailCredentials() {
        // Set default values
        this.host = DEFAULT_HOST;
        this.port = DEFAULT_PORT;
        this.email = DEFAULT_EMAIL;
        this.password = DEFAULT_PASSWORD;
    }

    public EmailCredentials(String host, int port, String email, String password) {
        this.host = host;
        this.port = port;
        this.email = email;
        this.password = password;
    }
}
