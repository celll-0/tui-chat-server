package com.cello;


import java.beans.ConstructorProperties;

public class Message {
    private final String clientId;
    public final MessageType type;
    private final String data;

    @ConstructorProperties({"clientId", "type", "data"})
    public Message(String clientId, MessageType type, String data) {
        this.clientId = clientId;
        this.type = type;
        this.data = data;
    }

    public String getClientId() {
        return clientId;
    }

    public String getData() {
        return data;
    }

    public enum MessageType
    {
        CHAT,
        ROI,
    }
}
