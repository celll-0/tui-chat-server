package com.cello;

import javax.print.attribute.standard.QueuedJobCount;
import java.io.BufferedReader;
import java.net.http.WebSocket;
import java.util.Queue;

public class IncomingsStreamHandler implements Runnable {
    private final BufferedReader in;
    private static Queue<String> messages;

    public IncomingsStreamHandler(BufferedReader in, Queue<String> msgsQueue) {
        this.in = in;
        this.messages = msgsQueue;
    }

    public void run(){

    }
}
