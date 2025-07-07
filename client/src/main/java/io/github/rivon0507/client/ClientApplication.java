package io.github.rivon0507.client;

import io.github.rivon0507.common.Message;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ClientApplication extends Application {
    private TextField serverField;
    private TextField portField;
    private TextField nameField;
    private Button connectButton;
    private Button disconnectButton;
    private TextField destinationField;
    private TextArea messageInput;
    private TextArea messageDisplay;
    private Label statusLabel;

    private MessageClient client;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Message Client");

        // Create UI components
        BorderPane root = new BorderPane();

        // Top connection panel
        GridPane connectionPanel = new GridPane();
        connectionPanel.setHgap(10);
        connectionPanel.setVgap(10);
        connectionPanel.setPadding(new Insets(10));

        connectionPanel.add(new Label("Server:"), 0, 0);
        serverField = new TextField("localhost");
        connectionPanel.add(serverField, 1, 0);

        connectionPanel.add(new Label("Port:"), 2, 0);
        portField = new TextField("8080");
        portField.setPrefWidth(80);
        connectionPanel.add(portField, 3, 0);

        connectionPanel.add(new Label("Your Name:"), 0, 1);
        nameField = new TextField();
        connectionPanel.add(nameField, 1, 1);

        connectButton = new Button("Connect");
        connectionPanel.add(connectButton, 2, 1);

        disconnectButton = new Button("Disconnect");
        disconnectButton.setDisable(true);
        connectionPanel.add(disconnectButton, 3, 1);

        // Center message display area
        messageDisplay = new TextArea();
        messageDisplay.setEditable(false);
        messageDisplay.setWrapText(true);
        VBox displayPanel = new VBox(5);
        displayPanel.setPadding(new Insets(10));
        Label displayLabel = new Label("Received Messages:");
        displayPanel.getChildren().addAll(displayLabel, messageDisplay);

        // Bottom message input area
        VBox inputPanel = new VBox(10);
        inputPanel.setPadding(new Insets(10));

        HBox destinationPanel = new HBox(10);
        destinationPanel.getChildren().addAll(new Label("Send to:"));

        destinationField = new TextField();
        destinationField.setPrefWidth(150);
        destinationField.setPromptText("Enter recipient name or 'ALL' for broadcast");
        destinationField.setText("ALL");
        destinationPanel.getChildren().add(destinationField);

        messageInput = new TextArea();
        messageInput.setWrapText(true);
        messageInput.setPrefHeight(100);

        Button sendButton = new Button("Send Message");
        sendButton.setOnAction(_ -> sendMessage());

        inputPanel.getChildren().addAll(destinationPanel, new Label("Message:"), messageInput, sendButton);

        // Status bar
        statusLabel = new Label("Disconnected");
        HBox statusBar = new HBox(statusLabel);
        statusBar.setPadding(new Insets(5));

        // Assemble layout
        root.setTop(connectionPanel);
        root.setCenter(displayPanel);
        root.setBottom(inputPanel);
        root.setBottom(new VBox(inputPanel, statusBar));

        // Setup actions
        connectButton.setOnAction(_ -> connect());
        disconnectButton.setOnAction(_ -> disconnect());

        // Create scene
        Scene scene = new Scene(root, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Handle application close
        primaryStage.setOnCloseRequest(_ -> {
            if (client != null && client.isConnected()) {
                client.disconnect("Application closed");
            }
        });
    }

    private void connect() {
        String serverHost = serverField.getText().trim();
        String clientName = nameField.getText().trim();

        if (clientName.isEmpty()) {
            updateStatus("Please enter your name");
            return;
        }

        try {
            int port = Integer.parseInt(portField.getText().trim());

            client = new MessageClient(serverHost, port, clientName);
            client.setMessageHandler(this::handleMessage);
            client.setConnectionStatusHandler(this::updateStatus);

            if (client.connect()) {
                // Update UI state
                connectButton.setDisable(true);
                disconnectButton.setDisable(false);
                serverField.setDisable(true);
                portField.setDisable(true);
                nameField.setDisable(true);
            }
        } catch (NumberFormatException e) {
            updateStatus("Invalid port number");
        }
    }

    private void disconnect() {
        if (client != null) {
            client.disconnect("User disconnected");
            client = null;
        }

        // Update UI state
        connectButton.setDisable(false);
        disconnectButton.setDisable(true);
        serverField.setDisable(false);
        portField.setDisable(false);
        nameField.setDisable(false);

        // Reset destination field to ALL
        Platform.runLater(() -> destinationField.setText("ALL"));
    }

    private void sendMessage() {
        if (client == null || !client.isConnected()) {
            updateStatus("Not connected to server");
            return;
        }

        String content = messageInput.getText().trim();
        if (content.isEmpty()) {
            updateStatus("Message cannot be empty");
            return;
        }

        String destination = destinationField.getText().trim();
        if (destination.isEmpty()) {
            updateStatus("Please enter a destination");
            return;
        }

        if (client.sendMessage(destination, content)) {
            messageInput.clear();
            // Show our sent message in the display area
            String prefix = destination.equals("ALL") ? "[To: EVERYONE]" : "[To: " + destination + "]";
            displayMessage(prefix + "\n" + content);
        }
    }

    private void handleMessage(Message message) {
        Platform.runLater(() -> displayMessage(message.toString()));
    }

    private void updateStatus(String status) {
        Platform.runLater(() -> statusLabel.setText(status));
    }

    private void displayMessage(String message) {
        messageDisplay.appendText(message + "\n\n");
    }
}
