package io.github.rivon0507.common;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String sender;
    private final String destination; // can be a specific client name or "ALL" for broadcast
    private final String content;

    public Message(String sender, String destination, String content) {
        this.sender = sender;
        this.destination = destination;
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public String getDestination() {
        return destination;
    }

    public String getContent() {
        return content;
    }

    public boolean isBroadcast() {
        return "ALL".equals(destination);
    }

    @Override
    public String toString() {
        return "From: " + sender + 
               (isBroadcast() ? " [BROADCAST]" : " [To: " + destination + "]") + 
               "\n" + content;
    }
}
