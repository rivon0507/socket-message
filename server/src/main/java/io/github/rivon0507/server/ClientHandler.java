package io.github.rivon0507.server;

import io.github.rivon0507.common.ConnectionRequest;
import io.github.rivon0507.common.ConnectionResponse;
import io.github.rivon0507.common.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final MessageServer server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String clientName;
    private boolean running = true;

    public ClientHandler(Socket socket, MessageServer server) {
        this.clientSocket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            // Set up input and output streams
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());

            // Read connection request with client name
            Object request = in.readObject();
            if (!(request instanceof ConnectionRequest)) {
                closeConnection("Invalid connection request");
                return;
            }

            // Process connection request
            clientName = ((ConnectionRequest) request).getClientName();

            // Check if name is already in use
            if (!server.registerClient(clientName, this)) {
                sendConnectionResponse(false, "Name already in use", server.getConnectedClients());
                closeConnection("Name already in use");
                return;
            }

            // Send success response with connected clients list
            sendConnectionResponse(true, "Connected successfully", server.getConnectedClients());

            // Announce new client to all clients
            server.broadcast(new Message("SERVER", "ALL", clientName + " has joined the chat."), null);

            // Handle incoming messages
            while (running) {
                Object received = in.readObject();
                if (received instanceof Message) {
                    Message message = (Message) received;
                    server.handleMessage(message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            server.removeClient(clientName);
            server.broadcast(new Message("SERVER", "ALL", clientName + " has left the chat."), null);
            closeConnection("Client disconnected");
        }
    }

    public void sendMessage(Message message) {
        try {
            if (out != null) {
                out.writeObject(message);
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("Error sending message to " + clientName + ": " + e.getMessage());
            closeConnection("Error sending message");
        }
    }

    public void sendConnectionResponse(boolean success, String message, List<String> clients) {
        try {
            out.writeObject(new ConnectionResponse(success, message, clients));
            out.flush();
        } catch (IOException e) {
            System.out.println("Error sending connection response: " + e.getMessage());
        }
    }

    private void closeConnection(String reason) {
        running = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
        System.out.println("Connection closed with " + clientName + ": " + reason);
    }

    public String getClientName() {
        return clientName;
    }
}
