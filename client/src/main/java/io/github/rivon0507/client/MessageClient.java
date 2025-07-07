package io.github.rivon0507.client;

import io.github.rivon0507.common.ConnectionRequest;
import io.github.rivon0507.common.ConnectionResponse;
import io.github.rivon0507.common.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

public class MessageClient {
    private final String serverHost;
    private final int serverPort;
    private final String clientName;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Thread listenerThread;
    private boolean connected = false;
    private Consumer<Message> messageHandler;
    private Consumer<String> connectionStatusHandler;

    public MessageClient(String serverHost, int serverPort, String clientName) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.clientName = clientName;
    }

    public boolean connect() {
        try {
            socket = new Socket(serverHost, serverPort);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Send connection request with client name
            out.writeObject(new ConnectionRequest(clientName));
            out.flush();

            // Read response
            Object response = in.readObject();
            if (!(response instanceof ConnectionResponse connResponse)) {
                disconnect("Invalid response from server");
                return false;
            }

            if (!connResponse.isSuccess()) {
                disconnect(connResponse.getMessage());
                return false;
            }

            // Connection successful

            // Start listener thread
            connected = true;
            startListener();

            // Notify status
            if (connectionStatusHandler != null) {
                connectionStatusHandler.accept("Connected to server");
            }

            return true;
        } catch (IOException | ClassNotFoundException e) {
            if (connectionStatusHandler != null) {
                connectionStatusHandler.accept("Connection error: " + e.getMessage());
            }
            disconnect("Error connecting to server");
            return false;
        }
    }

    public void disconnect(String reason) {
        connected = false;

        try {
            if (listenerThread != null) {
                listenerThread.interrupt();
                listenerThread = null;
            }
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.out.println("Error disconnecting: " + e.getMessage());
        }

        if (connectionStatusHandler != null) {
            connectionStatusHandler.accept("Disconnected: " + reason);
        }
    }

    public boolean sendMessage(String destination, String content) {
        if (!connected) {
            return false;
        }

        try {
            Message message = new Message(clientName, destination, content);
            out.writeObject(message);
            out.flush();
            return true;
        } catch (IOException e) {
            System.out.println("Error sending message: " + e.getMessage());
            disconnect("Error sending message");
            return false;
        }
    }

    private void startListener() {
        listenerThread = new Thread(() -> {
            try {
                while (connected) {
                    Object received = in.readObject();

                    if (received instanceof Message message) {
                        if (messageHandler != null) {
                            messageHandler.accept(message);
                        }
                    }  // Handle connection response if needed in the future

                }
            } catch (IOException | ClassNotFoundException e) {
                if (connected) {
                    disconnect("Connection lost: " + e.getMessage());
                }
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void setMessageHandler(Consumer<Message> handler) {
        this.messageHandler = handler;
    }

    public void setConnectionStatusHandler(Consumer<String> handler) {
        this.connectionStatusHandler = handler;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getClientName() {
        return clientName;
    }
}
