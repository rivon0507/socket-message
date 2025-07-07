package io.github.rivon0507.common;

import java.io.Serializable;

public class ConnectionRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String clientName;

    public ConnectionRequest(String clientName) {
        this.clientName = clientName;
    }

    public String getClientName() {
        return clientName;
    }
}
