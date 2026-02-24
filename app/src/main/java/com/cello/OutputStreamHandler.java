package com.cello;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.concurrent.BlockingQueue;


public  class OutputStreamHandler implements Runnable {
    private final BufferedWriter out;
    public final String clientId;
    private volatile BlockingQueue<String> outMessageQueue;
    private final ObjectMapper json;

    public OutputStreamHandler(OutputStream out, BlockingQueue<String> outMessageQueue, String clientId) {
        try {
            this.out = new BufferedWriter(new OutputStreamWriter(out));
            this.outMessageQueue = outMessageQueue;
            this.clientId = clientId;
            this.json = new ObjectMapper();
        } catch (Exception e) {
            System.err.println("Output stream initialization failed!" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try (out){
            while(true) {
                String inp = outMessageQueue.take();   // blocks until an item is available
                String jsonInp = json.writeValueAsString(new Message(clientId, Message.MessageType.CHAT, inp));
                out.write(jsonInp);
                out.newLine();
                out.flush();
            }
        }catch(Exception e) {
            System.err.println("Incoming stream had a fault: " + e.getClass().getSimpleName());
            throw new RuntimeException(e);
        } finally {
            try {
                out.close();
            } catch (IOException ignored){}
        }
    }
}
