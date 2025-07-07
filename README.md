# Socket Message Application

A minimal JavaFX application with client-server architecture for sending text messages between instances over a network using Java sockets.

## Features

- Client-server architecture with multiple client support
- Private messaging between clients
- Broadcasting messages to all connected clients
- JavaFX UI for both client and server
- Real-time message display
- Client connection management

## Project Structure

The project is organized into three Gradle modules:

- **common**: Contains shared classes used by both client and server
- **server**: The server application that manages client connections
- **client**: The client application for sending and receiving messages

## Running the Applications

### Starting the Server

```bash
./gradlew server:run
```

The server will start with a GUI that allows you to set the port and manage the server.

### Starting the Client

```bash
./gradlew client:run
```

The client UI will allow you to:
- Connect to a server by providing host, port, and your name
- Send messages to specific clients or broadcast to all
- View incoming messages

## Implementation Details

- Uses Java Socket API for network communication
- Serialized Java objects for message passing
- Multithreaded server for handling multiple clients
- JavaFX for the user interface
- Gradle for build management

## Requirements

- Java 22 or higher
- JavaFX (included through Gradle dependencies)
