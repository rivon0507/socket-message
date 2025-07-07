package io.github.rivon0507.server;

import io.github.rivon0507.common.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageServer {
    private final int port;
    private ServerSocket serverSocket;
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private boolean running = false;

    public MessageServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Server started on port " + port);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                pool.execute(clientHandler);
            }
        } catch (IOException e) {
            if (running) {
                System.out.println("Server error: " + e.getMessage());
            }
        } finally {
            stop();
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            pool.shutdown();
        } catch (IOException e) {
            System.out.println("Error stopping server: " + e.getMessage());
        }
        System.out.println("Server stopped");
    }

    public synchronized boolean registerClient(String clientName, ClientHandler handler) {
        if (clients.containsKey(clientName)) {
            return false;
        }
        clients.put(clientName, handler);
        System.out.println("Client registered: " + clientName);
        broadcastClientList();
        return true;
    }

    public synchronized void removeClient(String clientName) {
        clients.remove(clientName);
        System.out.println("Client removed: " + clientName);
        broadcastClientList();
    }

    public void broadcastClientList() {
        List<String> connectedClients = getConnectedClients();
        clients.forEach((name, handler) -> {
            handler.sendConnectionResponse(true, "Client list updated", connectedClients);
        });
    }

    public synchronized List<String> getConnectedClients() {
        return new ArrayList<>(clients.keySet());
    }

    public void handleMessage(Message message) {
        System.out.println("Message from " + message.getSender() + " to " + 
                          (message.isBroadcast() ? "ALL" : message.getDestination()));

        if (message.isBroadcast()) {
            broadcast(message, message.getSender());
        } else {
            // Send to specific client
            ClientHandler recipient = clients.get(message.getDestination());
            if (recipient != null) {
                recipient.sendMessage(message);
            } else {
                // Send error back to sender if destination client doesn't exist
                ClientHandler sender = clients.get(message.getSender());
                if (sender != null) {
                    sender.sendMessage(new Message("SERVER", message.getSender(), 
                                      "Error: Client '" + message.getDestination() + "' not found."));
                }
            }
        }
    }

    public void broadcast(Message message, String excludeClient) {
        clients.forEach((name, handler) -> {
            if (excludeClient == null || !name.equals(excludeClient)) {
                handler.sendMessage(message);
            }
        });
    }
}
