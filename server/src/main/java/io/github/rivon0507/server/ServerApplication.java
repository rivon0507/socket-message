package io.github.rivon0507.server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.OutputStream;
import java.io.PrintStream;

public class ServerApplication extends Application {
    private final TextArea logArea = new TextArea();
    private TextField portField;
    private Button startButton;
    private Button stopButton;
    private MessageServer server;
    private Thread serverThread;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Message Server");

        // Create UI components
        BorderPane root = new BorderPane();

        // Top control panel
        HBox controlPanel = new HBox(10);
        controlPanel.setPadding(new Insets(10));

        Label portLabel = new Label("Port:");
        portField = new TextField("8080");
        portField.setPrefWidth(80);

        startButton = new Button("Start Server");
        stopButton = new Button("Stop Server");
        stopButton.setDisable(true);

        controlPanel.getChildren().addAll(portLabel, portField, startButton, stopButton);

        // Center log area
        logArea.setEditable(false);
        logArea.setWrapText(true);
        VBox logPanel = new VBox(5);
        logPanel.setPadding(new Insets(10));
        Label logLabel = new Label("Server Log:");
        logPanel.getChildren().addAll(logLabel, logArea);

        root.setTop(controlPanel);
        root.setCenter(logPanel);

        // Setup actions
        startButton.setOnAction(e -> startServer());
        stopButton.setOnAction(e -> stopServer());

        // Redirect System.out to TextArea
        redirectSystemOut();

        // Create scene
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Handle application close
        primaryStage.setOnCloseRequest(e -> {
            if (server != null) {
                server.stop();
            }
            if (serverThread != null && serverThread.isAlive()) {
                serverThread.interrupt();
            }
        });
    }

    private void startServer() {
        try {
            int port = Integer.parseInt(portField.getText().trim());
            server = new MessageServer(port);

            // Run server in separate thread
            serverThread = new Thread(() -> server.start());
            serverThread.setDaemon(true);
            serverThread.start();

            // Update UI
            startButton.setDisable(true);
            stopButton.setDisable(false);
            portField.setDisable(true);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number");
        }
    }

    private void stopServer() {
        if (server != null) {
            server.stop();
            server = null;
        }

        // Update UI
        startButton.setDisable(false);
        stopButton.setDisable(true);
        portField.setDisable(false);
    }

    private void redirectSystemOut() {
        PrintStream printStream = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                appendText(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                appendText(new String(b, off, len));
            }
        });

        System.setOut(printStream);
        System.setErr(printStream);
    }

    private void appendText(String text) {
        Platform.runLater(() -> logArea.appendText(text));
    }
}
