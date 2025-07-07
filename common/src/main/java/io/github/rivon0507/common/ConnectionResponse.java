package io.github.rivon0507.common;

import java.io.Serializable;
import java.util.List;

public class ConnectionResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final List<String> connectedClients;

    public ConnectionResponse(boolean success, String message, List<String> connectedClients) {
        this.success = success;
        this.message = message;
        this.connectedClients = connectedClients;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getConnectedClients() {
        return connectedClients;
    }
}
