package com.cello;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.beans.ConstructorProperties;
import java.text.SimpleDateFormat;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {
    private final String clientId;
    public final MessageType type;
    private final String data;
    public final Date createdAt;

    @ConstructorProperties({"clientId", "type", "data", "createAt"})
    public Message(String clientId, MessageType type, String data) {
        this.clientId = clientId;
        this.type = type;
        this.data = data;
        this.createdAt = new Date();
    }

    public String getClientId() {
        return clientId;
    }

    public String getData() {
        return data;
    }

    public String getTimestamp(){
        return new SimpleDateFormat("MM-dd-yyyy|HH:mm:ss").format(createdAt);
    }

    public enum MessageType
    {
        CHAT,
        ROI,
    }
}
