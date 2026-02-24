package com.cello;


import com.fasterxml.jackson.core.JsonFactory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Server {
    private static final int PORT = 8000; // Port number for the server
    private static final Map<String, Socket> clients = new HashMap<String, Socket>();
    private static volatile BlockingQueue<String> outgoingMessageQueue = new ArrayBlockingQueue<String>(100);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);
            try {
                while (true) {
                    Socket connetion = serverSocket.accept();
                    AcceptClientThread acceptClient = new AcceptClientThread(connetion, outgoingMessageQueue);
                    clients.put(acceptClient.getClientId(), connetion);
                    acceptClient.start();
                }
            } catch (IOException e) {
                System.out.println("Error occured while waiting for new client connections!");
                System.out.println("Still connected: " + clients.size());
            }
        } catch (Exception e) {
            System.err.println("Server fault: " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static class AcceptClientThread extends Thread {
        private final Socket connection;
        private final String clientId;
        private final String clientHost;
        private final int clientPort;
        private volatile BlockingQueue<String> outMessageQueue;

        public AcceptClientThread(Socket connection, BlockingQueue<String> outgoingMessageQueue) {
            this.connection = connection;
            this.clientHost = connection.getInetAddress().getHostAddress();
            this.clientPort = connection.getPort();
            this.clientId = clientHost + "/" + clientPort;
            this.outMessageQueue = outgoingMessageQueue;
        }

        @Override
        public void run() {
            try {
                System.out.println("(" + clientId + ") connected");
                try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
                    InputStream in = connection.getInputStream();
                    OutputStream out = connection.getOutputStream();
                    Thread receiving = new Thread(new InputStreamHandler(in, terminal, outMessageQueue, clientId));
                    Thread sending = new Thread(new OutputStreamHandler(out, outMessageQueue, clientId));

                    receiving.start();
                    sending.start();

//                    Keep the threads alive
                    sending.join();
                    receiving.join();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    connection.close();
                    System.out.println("(" + clientId + ") disconnected");
                } catch (IOException e) {
                }
            }
        }

        public String getClientId() {
            return clientId;
        }

        public List<String> getConnectedClients(){
            List<String> connectedClients = clients.keySet().stream().toList();
            for (String connectedClient : connectedClients){
                System.out.println("[ " + connectedClient + " ]");
            }
            return connectedClients;
        }
    }
}